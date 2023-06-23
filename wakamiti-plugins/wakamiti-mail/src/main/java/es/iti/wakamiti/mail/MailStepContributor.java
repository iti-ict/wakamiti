package es.iti.wakamiti.mail;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.util.Properties;

@I18nResource("iti_wakamiti_wakamiti-mail")
@Extension(provider = "es.iti.wakamiti", name = "mail-steps")
public class MailStepContributor implements StepContributor {
    private String  protocol;
    private String host;
    private Integer port;
    private String address;
    private String password;
    private Session session;
    private Folder folder;
    private Store store;
    private Message[] messages;
    private String[] attachments = new String[]{};


    @Step(value = "mail.host", args = {"mail:text", "port:int", "protocol:text"})
    public void setHost(String host, Integer port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    @Step(value = "mail.login", args = {"address:text", "password:text"})
    public void setLogin(String address, String password) {
        this.address = address;
        this.password = password;
    }

    public void setStore(Store store) {
        this.store = store;
    }
    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

    @Step(value = "mail.message")
    public void getMessages() throws Exception {
        Properties props = new Properties();
        session = Session.getDefaultInstance(props, null);
        store = session.getStore(protocol);
        store.connect(host, address, password);
        Folder inboxFolder = openInboxFolder(store);
        messages = searchUnreadMails(inboxFolder);
        if (messages.length == 0) {
            throw new AssertionError("There is no message");
        }
    }

    public Session getSession(Properties properties) {
        if (this.session == null && properties != null) {
            this.session = Session.getInstance(properties);
            this.session.setDebug(true);
        }
        return this.session;
    }

    public Store connectToMailServer(Properties properties) {
        try{
            Session session = getSession(properties);
            Store store = session.getStore(protocol);
            store.connect(host, address, password);
            return store;
        } catch (Exception e){
            throw new AssertionError("The connection to the server could not be established");
        }
    }

    Folder openInboxFolder(Store store) throws MessagingException {
        Folder inboxFolder = store.getFolder("INBOX");
        inboxFolder.open(Folder.READ_WRITE);
        return inboxFolder;
    }

    Message[] searchUnreadMails(Folder folder) throws MessagingException {
        Flags seen = new Flags(Flags.Flag.SEEN);
        SearchTerm unseenFlagTerm = new FlagTerm(seen, false);
        messages = folder.search(unseenFlagTerm);
        return messages;
    }

    @Step(value = "mail.subject")
    public String getSubject() throws Exception {
        if (messages.length > 0) {
            return messages[0].getSubject();
        }
        throw new AssertionError("There is no message");
    }

    @Step(value = "mail.sender")
    public String getSender() throws Exception {
        if (messages.length > 0) {
            Address[] fromAddresses = messages[0].getFrom();
            if (fromAddresses.length > 0) {
                return fromAddresses[0].toString();
            }
        }
        throw new AssertionError("There is no message");
    }

    @Step(value = "mail.recipient")
    public String getRecipient() throws Exception {
        if (messages.length > 0) {
            Address[] toAddresses = messages[0].getRecipients(Message.RecipientType.TO);
            if (toAddresses.length > 0) {
                return toAddresses[0].toString();
            }
        }
        throw new AssertionError("There is no message");
    }

    @Step(value = "mail.body")
    public String getBody() throws Exception {
        if (messages.length > 0) {
            Object content = messages[0].getContent();
            if (content instanceof Multipart) {
                return extractTextFromMultipart((Multipart) content);
            } else if (content instanceof String) {
                return (String) content;
            }
        }
        throw new AssertionError("There is no message");
    }

    @Step(value = "mail.attached")
    public String getAttachment() throws Exception {
        try {
            if (messages == null || messages.length == 0) {
                return "";
            }
            StringBuilder attachments = new StringBuilder();
            for (Message message : messages) {
                if (message.getContent() instanceof Multipart) {
                    Multipart multipart = (Multipart) message.getContent();
                    String messageAttachments = findAttachments(multipart);
                    if (!messageAttachments.isEmpty()) {
                        attachments.append(messageAttachments);
                    }
                }
            }
            if (attachments.length() > 0) {
                attachments.setLength(attachments.length() - 2);
            }
            return attachments.toString();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private static String findAttachments(Multipart multipart) throws Exception {
        StringBuilder attachments = new StringBuilder();
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                String fileName = bodyPart.getFileName();
                attachments.append(fileName).append(", ");
            } else if (bodyPart.getContent() instanceof Multipart) {
                attachments.append(findAttachments((Multipart) bodyPart.getContent()));
            }
        }
        return attachments.toString();
    }


    private static String extractTextFromMultipart(Multipart multipart) throws Exception {
        StringBuilder sb = new StringBuilder();
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart instanceof MimeBodyPart) {
                MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                if (mimeBodyPart.isMimeType("text/plain")) {
                    sb.append(mimeBodyPart.getContent());
                } else if (mimeBodyPart.isMimeType("text/html")) {
                    String htmlContent = (String) mimeBodyPart.getContent();
                    sb.append(htmlContent);
                } else if (mimeBodyPart.getContent() instanceof Multipart) {
                    Multipart nestedMultipart = (Multipart) mimeBodyPart.getContent();
                    sb.append(extractTextFromMultipart(nestedMultipart));
                }
            }
        }
        return sb.toString();
    }

    @Step(value="mail.read")
    public void markAsRead() throws Exception {
        for (Message message : messages) {
            message.setFlag(Flags.Flag.SEEN, true);
        }
    }

    @TearDown()
    public void closeConnections() throws MessagingException {
        if (folder != null) {
            folder.close(false);
        }
        if (store != null) {
            store.close();
        }
    }

}
