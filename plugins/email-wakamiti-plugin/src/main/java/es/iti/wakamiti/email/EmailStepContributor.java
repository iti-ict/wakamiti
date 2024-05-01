package es.iti.wakamiti.email;

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
import org.hamcrest.*;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@I18nResource("iti_wakamiti_wakamiti-email")
@Extension(provider = "es.iti.wakamiti", name = "email-steps", version = "2.5")

public class EmailStepContributor implements StepContributor {


    private String storeProtocol;
    private String host;
    private Integer port;
    private String address;
    private String password;

    private String folder;

    private Message incomingMessage;

    private EmailHelper helper;

    private final List<Assertion<String>> cleanupFrom = new LinkedList<>();
    private final List<Assertion<String>> cleanupSubject = new LinkedList<>();


    private EmailHelper helper() {
        if (helper == null) {
            helper = new EmailHelper(storeProtocol,host,port,address,password);
        }
        return helper;
    }



    public void setStoreProtocol(String storeProtocol) {
        this.storeProtocol = storeProtocol;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setFolder(String folder) {
        this.folder = folder;
    }


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




    @Step(value = "email.define.host", args = {"host:text", "port:int", "protocol:word"})
    public void defineHost(String host, Integer port, String protocol) {
        this.host = host;
        this.port = port;
        this.storeProtocol = protocol;
    }


    @Step(value = "email.define.login", args = {"address:text", "password:text"})
    public void defineLogin(String address, String password) {
        this.address = address;
        this.password = password;
    }



    @Step(value = "email.define.folder")
    public void defineFolder(String folder) {
        this.folder = folder;
    }


    @Step(value= "email.assert.unread.messages", args = {"integer-assertion"})
    public void assertUnreadMessages(Assertion<Integer> assertion) {
        Assertion.assertThat(helper().getUnreadMessages(folder),assertion);
    }



    @Step(value = "email.assert.incoming.message", args = {"sec:integer"})
    public void assertIncomingMessage(Long seconds) {
       this.incomingMessage = helper().waitForIncomingMessage(folder, seconds);
    }


    @Step(value = "email.assert.subject", args = "text-assertion")
    public void assertSubject(Assertion<String> assertion) {
        assertMessage(Message::getSubject, assertion);
    }


    @Step(value = "email.assert.sender", args = "text-assertion")
    public void assertSender(Assertion<String> assertion) {
        assertMessage(message -> message.getFrom()[0].toString(), assertion);
    }


    @Step("email.assert.body")
    public void assertBody(Document body) {
        assertMessage(message -> helper().getBody(message), Matchers.equalTo(body.getContent()));
    }


    @Step("email.assert.body.partially")
    public void assertBodyPartially(Document body) {
        assertMessage(message -> helper().getBody(message), Matchers.containsString(body.getContent()));
    }


    @Step("email.assert.body.file")
    public void assertBodyFile(File file) {
        assertMessage(message -> helper().getBody(message), Matchers.equalTo(readFile(file)));
    }


    @Step("email.assert.body.file.partially")
    public void assertBodyFilePartially(File file) {
        assertMessage(message -> helper().getBody(message), Matchers.containsString(readFile(file)));
    }


    @Step(value = "email.assert.attachment.number", args = "integer-assertion")
    public void assertAttachmentNumber(Assertion<Integer> assertion) {
        assertMessage(message -> helper().getAllAttachments(message).size(), assertion);
    }


    @Step(value = "email.assert.attachment.name", args = "text-assertion")
    public void assertAttachmentName(Assertion<String> assertion) {
        try {
            Map.Entry<String, byte[]> attachment = helper().getFirstAttachment(currentMessage());
            Assertion.assertThat(attachment.getKey(), assertion);
        } catch (NoSuchElementException e) {
            throwNoAttachmentError();
        }
    }




    @Step("email.assert.attachment.content.binary.file")
    public void assertAttachmentBinaryFile(File file) {
        try {
            Map.Entry<String, byte[]> attachment = helper().getFirstAttachment(currentMessage());
            MatcherAssert.assertThat(attachment.getValue(), byteMatcher(readBinaryFile(file)));
        } catch (NoSuchElementException e) {
            throwNoAttachmentError();
        }
    }


    @Step("email.assert.attachment.content.text.file")
    public void assertAttachmentTextFile(File file) {
        try {
            Map.Entry<String, byte[]> attachment = helper().getFirstAttachment(currentMessage());
            MatcherAssert.assertThat(new String(attachment.getValue()), Matchers.equalTo(readFile(file)));
        } catch (NoSuchElementException e) {
            throwNoAttachmentError();
        }
    }


    @Step("email.assert.attachment.content.document")
    public void assertAttachmentDocument(Document document) {
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


    @Step(value = "email.cleanup.delete.emails.from", args = "text-assertion")
    public void cleanupDeleteEmailsFrom(Assertion<String> assertion) {
        this.cleanupFrom.add(assertion);
    }


    @Step(value = "email.cleanup.delete.emails.with.subject", args = "text-assertion")
    public void cleanupDeleteEmailsWithSubject(Assertion<String> assertion) {
        this.cleanupSubject.add(assertion);
    }





    private String readFile(File file) {
        return WakamitiAPI.instance().resourceLoader().readFileAsString(file);
    }



    private <T> void assertMessage(ThrowableFunction<Message,T> mapper, Matcher<T> matcher) {
        try {
            MatcherAssert.assertThat(mapper.apply(currentMessage()),matcher);
        } catch (RuntimeException e) {
            throw new WakamitiException(e);
        }
    }



    private <T> void assertMessage(ThrowableFunction<Message,T> mapper, Assertion<T> assertion) {
        try {
            Assertion.assertThat(mapper.apply(currentMessage()),assertion);
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
            throw new AssertionError("The email folder "+folder+" is empty");
        }
        return latestMessage;
    }



    private Matcher<byte[]> byteMatcher(byte[] bytes) {
        return new BaseMatcher<>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof byte[])) return false;
                return Arrays.equals((byte[]) o, bytes);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Byte contents do not match");
            }
        };
    }


    private byte[] readBinaryFile(File file) {
        try {
            return Files.readAllBytes(WakamitiAPI.instance().resourceLoader().absolutePath(file.toPath()));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }


}
