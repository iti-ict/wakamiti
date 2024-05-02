package es.iti.wakamiti.email.test;


import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.email.EmailConfigContributor;
import es.iti.wakamiti.email.EmailHelper;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
        @Property(key = EmailConfigContributor.STORE_HOST, value = "localhost"),
        @Property(key = EmailConfigContributor.STORE_PROTOCOL, value = "imap"),
        @Property(key = EmailConfigContributor.ADDRESS, value = "test@localhost"),
        @Property(key = EmailConfigContributor.PASSWORD, value = "test"),
        @Property(key = EmailConfigContributor.STORE_PORT, value = "2345"),
        @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.email.test.TestEmailSteps")
})
@I18nResource("test-steps")
public class TestEmailSteps implements StepContributor {


    static GreenMail mailServer;
    static GreenMailUser mailUser;
    static EmailHelper emailHelper;


    @BeforeClass
    public static void setUp() {
        ServerSetup smtpSetup = new ServerSetup(4321, null, "smtp");
        ServerSetup imapSetup = new ServerSetup(2345, null, "imap");
        mailServer = new GreenMail(new ServerSetup[]{smtpSetup, imapSetup});
        mailUser = mailServer.setUser("test@localhost", "test");
        mailServer.start();
        emailHelper = new EmailHelper("imap", "127.0.0.1", imapSetup.getPort(), "test@localhost", "test");
    }

    @AfterClass
    public static void tearDown() {
        emailHelper.close();
        mailServer.stop();
    }

    @Step("email.test.send.text.mail")
    public void sendTextMail() {
        GreenMailUtil.sendTextEmail(
                "test@localhost",
                "sender@localhost",
                "Test Subject",
                "Test Body",
                mailServer.getSmtp().getServerSetup()
        );
        wait(Durations.ONE_SECOND);
    }

    @Step("email.test.send.attachment.mail")
    public void sendAttachmentMail() {
        GreenMailUtil.sendAttachmentEmail(
                "test@localhost",
                "sender@localhost",
                "Test Subject",
                "Test Body",
                "Attachment".getBytes(),
                "text/plain",
                "attachment.txt",
                "Test attachment",
                mailServer.getSmtp().getServerSetup()
        );
        wait(Durations.ONE_SECOND);
    }

    private void wait(Duration duration) {
        Awaitility.await().pollDelay(duration).untilFalse(new AtomicBoolean());
    }

}
