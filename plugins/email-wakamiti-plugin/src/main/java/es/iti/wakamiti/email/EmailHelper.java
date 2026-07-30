/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.email;


import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * Provides the Email Helper functionality used by Wakamiti.
 */
public class EmailHelper {

    private static final String FORMAT = "[d' days 'H' hours 'm' minutes 's' seconds']";
    private static final Logger LOGGER = WakamitiLogger.forClass(EmailStepContributor.class);
    private Session session;
    private Store store;
    private final Map<String, Folder> folders = new HashMap<>();

    /**
     * Opens an authenticated JavaMail store connection.
     *
     * @param protocol store protocol such as {@code imap}, {@code imaps},
     *                 {@code pop3} or {@code pop3s}
     * @param host mail-server host
     * @param port explicit store port, or {@code null} for the protocol default
     * @param address mailbox login/address
     * @param password mailbox password
     * @throws WakamitiException when required settings are missing or the store
     *                           cannot connect
     */
    public EmailHelper(
            String protocol,
            String host,
            Integer port,
            String address,
            String password
    ) {
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

    private static Map<String, byte[]> findAttachments(
            Multipart multipart,
            int maxAttachments
    ) {
        try {
            Map<String, byte[]> attachments = new HashMap<>();
            for (int i = 0; i < multipart.getCount() && attachments.size() < maxAttachments; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
                        && bodyPart instanceof MimeBodyPart mimeBodyPart) {
                    attachments.put(bodyPart.getFileName(), readBytes(mimeBodyPart));
                }
            }
            return attachments;
        } catch (IOException | MessagingException e) {
            throw new WakamitiException(e);
        }
    }

    private static byte[] readBytes(
            MimeBodyPart bodyPart
    ) throws MessagingException, IOException {
        return bodyPart.getInputStream().readAllBytes();
    }

    /**
     * Expunges and closes every opened folder, then closes the mail store.
     * Folder-close failures are logged individually; a store-close failure is
     * propagated as a {@link WakamitiException}.
     */
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
                LOGGER.error("Cannot close email folder {} : {}", entry.getKey(), e.getMessage());
                LOGGER.debug("", e);
            }
        }
        folders.clear();
    }

    private Folder folder(
            String folderName
    ) {
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

    /**
     * Counts messages that do not have the JavaMail {@code SEEN} flag.
     *
     * @param folderName mailbox folder to search
     * @return number of unread messages
     */
    public Integer getUnreadMessages(
            String folderName
    ) {
        try {
            Flags seen = new Flags(Flags.Flag.SEEN);
            SearchTerm unseenFlagTerm = new FlagTerm(seen, false);
            return folder(folderName).search(unseenFlagTerm).length;
        } catch (MessagingException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Finds the newest non-deleted message in a folder.
     *
     * @param folderName mailbox folder to inspect
     * @return latest available message, or {@code null} when the folder is empty
     */
    public Message getLatestMessage(
            String folderName
    ) {
        try {
            Folder folder = folder(folderName);
            for (int i = folder.getMessageCount(); i > 0; i--) {
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

    /**
     * Waits for JavaMail to report a newly added message and returns the final
     * message in the folder.
     *
     * @param folderName mailbox folder to observe
     * @param duration maximum wait
     * @return message added during the wait
     * @throws org.awaitility.core.ConditionTimeoutException if no addition is
     *                                                        observed in time
     */
    public Message waitForIncomingMessage(
            String folderName,
            Duration duration
    ) {
        Folder folder = folder(folderName);
        AtomicBoolean received = new AtomicBoolean();
        MessageCountListener listener = new MessageCountAdapter() {
            @Override
            public void messagesAdded(
                    MessageCountEvent e
            ) {
                received.set(true);
            }
        };
        folder.addMessageCountListener(listener);
        try {
            Awaitility.await().atMost(duration).pollDelay(Durations.ONE_SECOND).untilTrue(received);
            if (!received.get()) {
                throw new AssertionError("No new email messages received within " + formatDuration(duration.toMillis(), FORMAT));
            }
            return folder.getMessage(folder.getMessageCount());
        } catch (MessagingException e) {
            throw new WakamitiException(e);
        } finally {
            folder.removeMessageCountListener(listener);
        }
    }

    /**
     * Extracts all MIME parts explicitly marked as attachments.
     *
     * @param message message to inspect
     * @return attachment bytes keyed by original file name, or an empty map for
     *         a non-multipart message
     */
    public Map<String, byte[]> getAllAttachments(
            Message message
    ) {
        try {
            if (message.getContent() instanceof Multipart multipart) {
                return findAttachments(multipart, Integer.MAX_VALUE);
            } else {
                return Map.of();
            }
        } catch (IOException | MessagingException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Extracts the first MIME attachment encountered.
     *
     * @param message message to inspect
     * @return attachment file name and bytes
     * @throws NoSuchElementException when the message has no attachment
     */
    public Map.Entry<String, byte[]> getFirstAttachment(
            Message message
    ) {
        try {
            if (message.getContent() instanceof Multipart multipart) {
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

    /**
     * Extracts textual message content.
     * <p>
     * Plain string content is returned directly. For multipart messages, all
     * parts without a disposition are concatenated in MIME order, excluding
     * attachments.
     *
     * @param message message to inspect
     * @return extracted body text
     * @throws WakamitiException when the content type cannot be interpreted
     */
    public String getBody(
            Message message
    ) {
        try {
            Object content = message.getContent();
            if (content instanceof String string) {
                return string;
            }
            if (message.getContent() instanceof Multipart multipart) {
                StringBuilder body = new StringBuilder();
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

    /**
     * Marks every message satisfying a condition as deleted.
     * Deletion is expunged when {@link #close()} closes the folder.
     *
     * @param folderName mailbox folder to scan
     * @param condition predicate evaluated for each message
     */
    public void deleteMessages(
            String folderName,
            ThrowableFunction<Message, Boolean> condition
    ) {
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

    /**
     * Returns every message currently visible in a folder.
     *
     * @param folderName mailbox folder to read
     * @return messages in JavaMail folder order
     * @throws MessagingException if the server cannot enumerate messages
     */
    public Message[] getAllMessages(
            String folderName
    ) throws MessagingException {
        return folder(folderName).getMessages();
    }

}
