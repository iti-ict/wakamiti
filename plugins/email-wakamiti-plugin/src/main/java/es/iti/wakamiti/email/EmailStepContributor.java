/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.email;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.mail.Message;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.ThrowableFunction;


/**
 * Provides the Email Step Contributor functionality used by Wakamiti.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "email-steps",
        version = "2.13"
)
@I18nResource("iti_wakamiti_wakamiti-email")
public class EmailStepContributor implements StepContributor {

    private final List<Assertion<String>> cleanupFrom = new LinkedList<>();
    private final List<Assertion<String>> cleanupSubject = new LinkedList<>();
    private String storeProtocol;
    private String host;
    private Integer port;
    private String address;
    private String password;
    private String folder;
    private Message incomingMessage;
    private EmailHelper helper;

    private EmailHelper helper() {
        if (helper == null) {
            helper = new EmailHelper(storeProtocol, host, port, address, password);
        }
        return helper;
    }

    /**
     * @param storeProtocol JavaMail store protocol, for example {@code imaps}
     */
    public void setStoreProtocol(
            String storeProtocol
    ) {
        this.storeProtocol = storeProtocol;
    }

    /**
     * @param host incoming-mail server host
     */
    public void setHost(
            String host
    ) {
        this.host = host;
    }

    /**
     * @param port explicit incoming-mail port, or {@code null} for the protocol default
     */
    public void setPort(
            Integer port
    ) {
        this.port = port;
    }

    /**
     * @param address mailbox address/login
     */
    public void setAddress(
            String address
    ) {
        this.address = address;
    }

    /**
     * @param password mailbox password
     */
    public void setPassword(
            String password
    ) {
        this.password = password;
    }

    /**
     * @param folder mailbox folder used by subsequent steps
     */
    public void setFolder(
            String folder
    ) {
        this.folder = folder;
    }

    /**
     * Applies deferred sender/subject cleanup rules and closes the mail session.
     * Matching messages are marked deleted and expunged as the folder closes.
     */
    @TearDown
    public void close() {
        try {
            for (Assertion<String> cleanup : this.cleanupSubject) {
                helper().deleteMessages(folder, message -> cleanup.test(message.getSubject()));
            }
            for (Assertion<String> cleanup : this.cleanupFrom) {
                helper().deleteMessages(folder, message -> cleanup.test(message.getFrom()[0].toString()));
            }
            this.incomingMessage = null;
            this.cleanupFrom.clear();
            this.cleanupSubject.clear();
        } finally {
            helper().close();
        }
    }

    /**
     * Defines the incoming-mail endpoint used for lazy connection.
     *
     * @param host mail-server host
     * @param port store port
     * @param protocol JavaMail store protocol
     */
    @Step(value = "email.define.host", args = {"host:text", "port:int", "protocol:word"})
    public void defineHost(
            String host,
            Integer port,
            String protocol
    ) {
        this.host = host;
        this.port = port;
        this.storeProtocol = protocol;
    }

    /**
     * Defines mailbox credentials used when the connection is first needed.
     *
     * @param address mailbox address/login
     * @param password mailbox password
     */
    @Step(value = "email.define.login", args = {"address:text", "password:text"})
    public void defineLogin(
            String address,
            String password
    ) {
        this.address = address;
        this.password = password;
    }

    /** @param folder mailbox folder used by subsequent search and assertion steps */
    @Step(value = "email.define.folder")
    public void defineFolder(
            String folder
    ) {
        this.folder = folder;
    }

    /**
     * Applies an assertion to the number of messages without the {@code SEEN}
     * flag in the selected folder.
     *
     * @param assertion condition applied to the unread count
     */
    @Step(value = "email.assert.unread.messages", args = {"integer-assertion"})
    public void assertUnreadMessages(
            Assertion<Integer> assertion
    ) {
        Assertion.assertThat(helper().getUnreadMessages(folder), assertion);
    }

    /**
     * Waits for a new message and makes it the subject of later assertions.
     *
     * @param duration maximum time to wait
     */
    @Step(value = "email.assert.incoming.message", args = {"duration:duration"})
    public void assertIncomingMessage(
            Duration duration
    ) {
        this.incomingMessage = helper().waitForIncomingMessage(folder, duration);
    }

    /**
     * @param assertion condition applied to the selected message subject
     */
    @Step(value = "email.assert.subject", args = "text-assertion")
    public void assertSubject(
            Assertion<String> assertion
    ) {
        assertMessage(Message::getSubject, assertion);
    }

    /**
     * @param assertion condition applied to the first sender address
     */
    @Step(value = "email.assert.sender", args = "text-assertion")
    public void assertSender(
            Assertion<String> assertion
    ) {
        assertMessage(message -> message.getFrom()[0].toString(), assertion);
    }

    /**
     * Requires the extracted message body to equal document content exactly.
     *
     * @param body expected body text
     */
    @Step("email.assert.body")
    public void assertBody(
            Document body
    ) {
        assertMessage(message -> helper().getBody(message), Matchers.equalTo(body.getContent()));
    }

    /**
     * Requires the extracted message body to contain document content.
     *
     * @param body expected body fragment
     */
    @Step("email.assert.body.partially")
    public void assertBodyPartially(
            Document body
    ) {
        assertMessage(message -> helper().getBody(message), Matchers.containsString(body.getContent()));
    }

    /**
     * Requires the extracted body to equal a text file's decoded content.
     *
     * @param file expected text file resolved by Wakamiti
     */
    @Step("email.assert.body.file")
    public void assertBodyFile(
            File file
    ) {
        assertMessage(message -> helper().getBody(message), Matchers.equalTo(readFile(file)));
    }

    /**
     * Requires the extracted body to contain a text file's decoded content.
     *
     * @param file file containing the expected body fragment
     */
    @Step("email.assert.body.file.partially")
    public void assertBodyFilePartially(
            File file
    ) {
        assertMessage(message -> helper().getBody(message), Matchers.containsString(readFile(file)));
    }

    /**
     * @param assertion condition applied to the MIME attachment count
     */
    @Step(value = "email.assert.attachment.number", args = "integer-assertion")
    public void assertAttachmentNumber(
            Assertion<Integer> assertion
    ) {
        assertMessage(message -> helper().getAllAttachments(message).size(), assertion);
    }

    /**
     * Applies a text assertion to the first attachment's original file name.
     *
     * @param assertion expected attachment-name condition
     * @throws AssertionError when no attachment exists
     */
    @Step(value = "email.assert.attachment.name", args = "text-assertion")
    public void assertAttachmentName(
            Assertion<String> assertion
    ) {
        try {
            Map.Entry<String, byte[]> attachment = helper().getFirstAttachment(currentMessage());
            Assertion.assertThat(attachment.getKey(), assertion);
        } catch (NoSuchElementException e) {
            throwNoAttachmentError();
        }
    }

    /**
     * Byte-compares the first attachment with a local binary file.
     *
     * @param file expected binary file
     * @throws AssertionError when no attachment exists or bytes differ
     */
    @Step("email.assert.attachment.content.binary.file")
    public void assertAttachmentBinaryFile(
            File file
    ) {
        try {
            Map.Entry<String, byte[]> attachment = helper().getFirstAttachment(currentMessage());
            MatcherAssert.assertThat(attachment.getValue(), byteMatcher(readBinaryFile(file)));
        } catch (NoSuchElementException e) {
            throwNoAttachmentError();
        }
    }

    /**
     * Text-compares the first attachment with a local file.
     *
     * @param file expected text file
     * @throws AssertionError when no attachment exists or text differs
     */
    @Step("email.assert.attachment.content.text.file")
    public void assertAttachmentTextFile(
            File file
    ) {
        try {
            Map.Entry<String, byte[]> attachment = helper().getFirstAttachment(currentMessage());
            MatcherAssert.assertThat(new String(attachment.getValue()), Matchers.equalTo(readFile(file)));
        } catch (NoSuchElementException e) {
            throwNoAttachmentError();
        }
    }

    /**
     * Text-compares the first attachment with a Wakamiti document.
     *
     * @param document expected attachment text
     * @throws AssertionError when no attachment exists or text differs
     */
    @Step("email.assert.attachment.content.document")
    public void assertAttachmentDocument(
            Document document
    ) {
        try {
            Map.Entry<String, byte[]> attachment = helper().getFirstAttachment(currentMessage());
            MatcherAssert.assertThat(new String(attachment.getValue()), Matchers.equalTo(document.getContent()));
        } catch (NoSuchElementException e) {
            throwNoAttachmentError();
        }
    }

    private void throwNoAttachmentError() {
        throw new AssertionError("The email has no attachments");
    }

    /**
     * Defers deletion of messages whose first sender matches an assertion until
     * teardown.
     *
     * @param assertion sender condition
     */
    @Step(value = "email.cleanup.delete.emails.from", args = "text-assertion")
    public void cleanupDeleteEmailsFrom(
            Assertion<String> assertion
    ) {
        this.cleanupFrom.add(assertion);
    }

    /**
     * Defers deletion of messages whose subject matches an assertion until
     * teardown.
     *
     * @param assertion subject condition
     */
    @Step(value = "email.cleanup.delete.emails.with.subject", args = "text-assertion")
    public void cleanupDeleteEmailsWithSubject(
            Assertion<String> assertion
    ) {
        this.cleanupSubject.add(assertion);
    }

    private String readFile(
            File file
    ) {
        return WakamitiAPI.instance().resourceLoader().readFileAsString(file);
    }

    private <T> void assertMessage(
            ThrowableFunction<Message, T> mapper,
            Matcher<T> matcher
    ) {
        try {
            MatcherAssert.assertThat(mapper.apply(currentMessage()), matcher);
        } catch (RuntimeException e) {
            throw new WakamitiException(e);
        }
    }

    private <T> void assertMessage(
            ThrowableFunction<Message, T> mapper,
            Assertion<T> assertion
    ) {
        try {
            Assertion.assertThat(mapper.apply(currentMessage()), assertion);
        } catch (RuntimeException e) {
            throw new WakamitiException(e);
        }
    }

    private Message currentMessage() {
        if (incomingMessage != null) {
            return incomingMessage;
        }
        Message latestMessage = helper().getLatestMessage(folder);
        if (latestMessage == null) {
            throw new AssertionError("The email folder " + folder + " is empty");
        }
        return latestMessage;
    }

    private Matcher<byte[]> byteMatcher(
            byte[] bytes
    ) {
        return new BaseMatcher<>() {
            @Override
            public boolean matches(
                    Object o
            ) {
                if (!(o instanceof byte[])) {
                    return false;
                }
                return Arrays.equals((byte[]) o, bytes);
            }

            @Override
            public void describeTo(
                    Description description
            ) {
                description.appendText("Byte contents do not match");
            }
        };
    }

    private byte[] readBinaryFile(
            File file
    ) {
        try {
            return Files.readAllBytes(WakamitiAPI.instance().resourceLoader().absolutePath(file.toPath()));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

}
