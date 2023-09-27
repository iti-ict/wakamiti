package es.iti.wakamiti.email;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class EmailHelper {

    private static Logger logger = WakamitiLogger.forClass(EmailStepContributor.class);


    private Session session;
    private Store store;
    private Map<String, Folder> folders = new HashMap<>();


    public EmailHelper(String protocol, String host, Integer port, String address, String password) {
        try {
            Objects.requireNonNull(protocol, "Email store protocol is not defined");
            Objects.requireNonNull(host, "Email host is not defined");
            Objects.requireNonNull(address, "Email address is not defined");
            Objects.requireNonNull(password, "Email password is not defined");
            this.session = Session.getDefaultInstance(new Properties(), null);
            this.store = session.getStore(protocol);
            if (port == null) {
                this.store.connect(host, address, password);
            } else {
                this.store.connect(host, port, address, password);
            }
        } catch (MessagingException | NullPointerException e) {
            throw new WakamitiException(e);
        }
    }


    public void close() {
        try {
            closeFolders();
            if (this.store != null) {
                this.store.close();
                this.store = null;
            }
            if (this.session != null) {
                this.session = null;
            }
        } catch (MessagingException e) {
            throw new WakamitiException("Error closing email session", e);
        }
    }


    private void closeFolders() {
        for (Map.Entry<String, Folder> entry : folders.entrySet()) {
            try {
                entry.getValue().close(true);
            } catch (MessagingException e) {
                logger.error("Cannot close email folder {} : {}", entry.getKey(), e.getMessage());
                logger.debug("", e);
            }
        }
        folders.clear();
    }


    private Folder folder(String folderName) {

        if (folderName == null) {
            throw new WakamitiException("Email folder not defined");
        }

        if (this.folders.containsKey(folderName)) {
            return folders.get(folderName);
        }
        try {
            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_WRITE);
            this.folders.put(folderName, folder);
            return folder;
        } catch (MessagingException e) {
            throw new WakamitiException("Cannot open email folder {}", folderName, e);
        }
    }


    public Integer getUnreadMessages(String folderName) {
        try {
            Flags seen = new Flags(Flags.Flag.SEEN);
            SearchTerm unseenFlagTerm = new FlagTerm(seen, false);
            return folder(folderName).search(unseenFlagTerm).length;
        } catch (MessagingException e) {
            throw new WakamitiException(e);
        }
    }


    public Message getLatestMessage(String folderName) {
        try {
            Folder folder = folder(folderName);
            for (int i=folder.getMessageCount(); i>0; i--) {
                Message message = folder.getMessage(i);
                if (!message.isSet(Flags.Flag.DELETED)) {
                    return message;
                }
            }
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        } catch (MessagingException e) {
            throw new WakamitiException(e);
        }
    }


    public Message waitForIncomingMessage(String folderName, long seconds) {

        Folder folder = folder(folderName);
        AtomicBoolean received = new AtomicBoolean();
        MessageCountListener listener = new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                received.set(true);
            }
        };
        folder.addMessageCountListener(listener);
        try {

            Awaitility.await().atMost(seconds, TimeUnit.SECONDS).pollDelay(Duration.ofSeconds(1L)).untilTrue(received);
            if (!received.get()) {
                throw new AssertionError("No new email messages received within " + seconds + " seconds");
            }
            return folder.getMessage(folder.getMessageCount());

        } catch (MessagingException e) {
            throw new WakamitiException(e);
        } finally {
            folder.removeMessageCountListener(listener);
        }

    }


    public Map<String, byte[]> getAllAttachments(Message message) {
        try {
            if (message.getContent() instanceof Multipart) {
                Multipart multipart = (Multipart) message.getContent();
                return findAttachments(multipart, Integer.MAX_VALUE);
            } else {
                return Map.of();
            }
        } catch (IOException | MessagingException e) {
            throw new WakamitiException(e);
        }
    }


    public Map.Entry<String, byte[]> getFirstAttachment(Message message) {
        try {
            if (message.getContent() instanceof Multipart) {
                Multipart multipart = (Multipart) message.getContent();
                Iterator<Map.Entry<String, byte[]>> iterator = findAttachments(multipart, 1).entrySet().iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                }
            }
            throw new NoSuchElementException();
        } catch (IOException | MessagingException e) {
            throw new WakamitiException(e);
        }
    }


    public String getBody(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            }
            if (message.getContent() instanceof Multipart) {
                StringBuilder body = new StringBuilder();
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (bodyPart.getDisposition() == null) {
                        body.append(bodyPart.getContent());
                    }
                }
                return body.toString();
            }
            throw new WakamitiException("Cannot extract email body of type {}", content.getClass());
        } catch (IOException | MessagingException e) {
            throw new WakamitiException(e);
        }
    }


    private static Map<String, byte[]> findAttachments(Multipart multipart, int maxAttachments) {
        try {
            Map<String, byte[]> attachments = new HashMap<>();
            for (int i = 0; i < multipart.getCount() && attachments.size() < maxAttachments; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && bodyPart instanceof MimeBodyPart) {
                    attachments.put(bodyPart.getFileName(), readBytes(((MimeBodyPart) bodyPart)));
                }
            }
            return attachments;
        } catch (IOException | MessagingException e) {
            throw new WakamitiException(e);
        }
    }


    public void deleteMessages(String folderName, ThrowableFunction<Message, Boolean> condition) {
        try {
            Folder folder = folder(folderName);
            for (int i = 1; i <= folder.getMessageCount(); i++) {
                Message message = folder.getMessage(i);
                if (Boolean.TRUE.equals(condition.apply(message))) {
                    message.setFlag(Flags.Flag.DELETED, true);
                }
            }
        } catch (MessagingException e) {
            throw new WakamitiException(e);
        }
    }



    private static byte[] readBytes(MimeBodyPart bodyPart) throws MessagingException, IOException {
        return bodyPart.getInputStream().readAllBytes();
    }


    public Message[] getAllMessages(String folderName) throws MessagingException {
        return folder(folderName).getMessages();
    }
}