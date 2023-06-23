package es.iti.wakamiti.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class MockMailStepContributor extends MailStepContributor {
    private List<Message> messages;

    @Override
    public Store connectToMailServer(Properties properties) {
        Session session = Session.getInstance(properties);
        return new MockStore(session, null);
    }


    @Override
    public Folder openInboxFolder(Store store) {
        return null;
    }

    public Message[] searchUnreadMails(Folder folder) throws MessagingException {
        messages = new ArrayList<>();
        Message[] allMessages = folder.getMessages();
        for (Message message : allMessages) {
            if (!message.isSet(Flags.Flag.SEEN)) {
                messages.add(message);
            }
        }
        return allMessages;
    }

    public void setMessages(Message[] messages) {
        this.messages = new ArrayList<>();
        for (Message message : messages) {
            this.messages.add(message);
        }
    }

    public String getSubject() throws MessagingException {
        if (messages != null && messages.size() > 0) {
            return messages.get(0).getSubject();
        }
        return null;
    }

    public String getSender() throws MessagingException {
        if (messages != null && messages.size() > 0) {
            Address[] from = messages.get(0).getFrom();
            if (from != null && from.length > 0) {
                return ((InternetAddress) from[0]).getAddress();
            }
        }
        return null;
    }

    public String getRecipient() throws MessagingException {
        if (messages != null && messages.size() > 0) {
            Address[] to = messages.get(0).getRecipients(Message.RecipientType.TO);
            if (to != null && to.length > 0) {
                return ((InternetAddress) to[0]).getAddress();
            }
        }
        return null;
    }

    public String getBody() throws MessagingException, IOException {
        if (messages != null && messages.size() > 0) {
            Object content = messages.get(0).getContent();
            if (content instanceof String) {
                return (String) content;
            }
        }
        return null;
    }

    public String getAttachment() throws MessagingException, IOException {
        if (messages != null && messages.size() > 0) {
            Message message = messages.get(0);
            Multipart multipart = (Multipart) message.getContent();
            StringBuilder attachmentNames = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String fileName = bodyPart.getFileName();
                    if (fileName != null) {
                        attachmentNames.append(fileName).append(", ");
                    }
                }
            }
            if (attachmentNames.length() > 0) {
                attachmentNames.delete(attachmentNames.length() - 2, attachmentNames.length());
            }
            return attachmentNames.toString();
        }
        return null;
    }

    public void markAsRead() throws MessagingException {
        if (messages != null) {
            for (Message message : messages) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
        }
    }


    public static class MockStore extends Store implements es.iti.wakamiti.mail.MockStore {

        public MockStore(Session session, URLName urlname) {
            super(session, urlname);
        }

        @Override
        public Folder getDefaultFolder() {
            return new MockStore.MockFolder(this);
        }

        @Override
        public Folder getFolder(String name) {
            return new MockStore.MockFolder(this);
        }

        @Override
        public Folder getFolder(URLName url) {
            return new MockStore.MockFolder(this);
        }

        @Override
        public Folder[] getPersonalNamespaces() {
            return new Folder[0];
        }

        @Override
        public Folder[] getUserNamespaces(String user) {
            return new Folder[0];
        }

        @Override
        public Folder[] getSharedNamespaces() {
            return new Folder[0];
        }

        @Override
        public Folder[] getSharedNamespaces(String user) {
            return new Folder[0];
        }

        @Override
        public Folder getDefaultFolderNewStyle() {
            return new MockStore.MockFolder(this);
        }

        @Override
        public Folder getFolder(URLName arg0, String arg1) {
            return new MockStore.MockFolder(this);
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void connect() {

        }

        private static class MockFolder extends Folder {
            private Message[] messages;
            private boolean isOpen = false;

            public MockFolder(Store store) {
                super(store);
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getFullName() {
                return null;
            }

            @Override
            public Folder getParent() {
                return null;
            }

            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public Folder[] list(String s) {
                return new Folder[0];
            }

            @Override
            public boolean isOpen() {
                return isOpen;
            }

            @Override
            public Flags getPermanentFlags() {
                return null;
            }

            @Override
            public int getMessageCount() {
                return 0;
            }

            @Override
            public void open(int mode) {
                if (!isOpen) {
                    isOpen = true;
                }
            }

            @Override
            public void close(boolean expunge) {
                if (isOpen) {
                    isOpen = false;
                }
            }

            @Override
            public Message[] getMessages() throws MessagingException {
                checkOpen();
                return messages != null ? messages : new Message[0];
            }

            @Override
            public Message getMessage(int msgnum) throws MessagingException {
                checkOpen();
                if (messages != null && msgnum >= 1 && msgnum <= messages.length) {
                    return messages[msgnum - 1];
                }
                return null;
            }

            public void setMessages(Message[] messages) {
                this.messages = messages;
            }

            private void checkOpen() throws MessagingException {
                if (!isOpen) {
                    throw new MessagingException("Folder not open");
                }
            }

            @Override
            public Folder[] listSubscribed(String pattern) {
                return new Folder[0];
            }

            @Override
            public char getSeparator() {
                return 0;
            }

            @Override
            public int getType() {
                return 0;
            }

            @Override
            public boolean create(int type) {
                return true;
            }

            @Override
            public boolean hasNewMessages() {
                return false;
            }

            @Override
            public Folder getFolder(String s) {
                return null;
            }

            @Override
            public boolean delete(boolean recurse) {
                return true;
            }

            @Override
            public boolean renameTo(Folder folder) {
                return false;
            }

            @Override
            public void appendMessages(Message[] msgs) {

            }

            @Override
            public Message[] expunge() {
                return new Message[0];
            }
        }

    }}
