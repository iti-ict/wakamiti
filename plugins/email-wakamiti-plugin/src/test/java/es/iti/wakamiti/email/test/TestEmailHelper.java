package es.iti.wakamiti.email.test;


import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import es.iti.wakamiti.email.EmailHelper;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.*;
import java.time.Duration;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class TestEmailHelper {


    static GreenMail mailServer;
    static GreenMailUser mailUser;

    EmailHelper emailHelper;


    @BeforeClass
    public static void setUp() throws IOException {
        ServerSetup smtpSetup = new ServerSetup(freePort(), null, "smtp");
        ServerSetup imapSetup = new ServerSetup(freePort(), null, "imap");
        mailServer = new GreenMail(new ServerSetup[]{smtpSetup,imapSetup});
        mailUser = mailServer.setUser("test@localhost","test");
        mailServer.start();
    }



    @AfterClass
    public static void tearDown() {
        mailServer.stop();
    }


    @After
    public void purgeEmail() throws FolderException {
        emailHelper.close();
        mailServer.purgeEmailFromAllMailboxes();
    }


    private EmailHelper emailHelper() {
        return new EmailHelper("imap", "127.0.0.1", mailServer.getImap().getPort(), "test@localhost", "test");
    }


    @Test
    public void lastSentTextMessageCanBeRead() throws MessagingException, InterruptedException {
        emailHelper = emailHelper();
        printCurrentMessages();
        GreenMailUtil.sendTextEmail(
            "test@localhost",
            "sender@localhost",
            "Test Subject 1",
            "Test Body 1",
            mailServer.getSmtp().getServerSetup()
        );
        wait(Durations.ONE_SECOND);
        printCurrentMessages();
        Message message = emailHelper.getLatestMessage("INBOX");
        Assert.assertNotNull(message);
        Assert.assertEquals("Test Subject 1",message.getSubject());
        Assert.assertEquals("sender@localhost", message.getFrom()[0].toString());

    }




    @Test
    public void lastSentMessageWithAttachmentCanBeRead() throws MessagingException {
        emailHelper = emailHelper();
        GreenMailUtil.sendAttachmentEmail(
            "test@localhost",
            "sender@localhost",
            "Test Subject 2",
            "Test Body 2",
            "Attachment".getBytes(),
            "text/plain",
            "attachment.txt",
            "Test attachment",
            mailServer.getSmtp().getServerSetup()
        );
        wait(Durations.ONE_HUNDRED_MILLISECONDS);
        Message message = emailHelper.getLatestMessage("INBOX");
        Assert.assertNotNull(message);
        Assert.assertEquals("Test Subject 2",message.getSubject());
        Assert.assertEquals("sender@localhost", message.getFrom()[0].toString());
        Assert.assertEquals("attachment.txt",emailHelper.getFirstAttachment(message).getKey());
    }



    @Test
    public void messagesCanBeDeleted() throws MessagingException {
        emailHelper = emailHelper();
        GreenMailUtil.sendTextEmail(
            "test@localhost",
            "sender@localhost",
            "Test Subject 3",
            "Test Body 3",
            mailServer.getSmtp().getServerSetup()
        );
        GreenMailUtil.sendTextEmail(
            "test@localhost",
            "sender@localhost",
            "Test Subject 4",
            "Test Body 4",
            mailServer.getSmtp().getServerSetup()
        );
        wait(Durations.ONE_HUNDRED_MILLISECONDS);
        Assert.assertEquals(2, mailServer.getReceivedMessages().length);
        Assert.assertEquals(0L, Stream.of(mailServer.getReceivedMessages()).filter(this::hasFlagDeleted).count());
        emailHelper.deleteMessages("INBOX", message -> message.getSubject().startsWith("Test Subject 4"));
        Assert.assertEquals(1L, Stream.of(mailServer.getReceivedMessages()).filter(this::hasFlagDeleted).count());

    }


    private static int freePort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }



    private boolean hasFlagDeleted(Message message) {
        try {
            return message.getFlags().contains(Flags.Flag.DELETED);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void wait(Duration duration) {
        Awaitility.await().pollDelay(duration).untilFalse(new AtomicBoolean());
    }


    private void printCurrentMessages() throws MessagingException {
        Message[] messages = emailHelper.getAllMessages("INBOX");
        if (messages.length == 0)  {
            System.out.println("INBOX empty");
        }
        for (int i = 0; i < messages.length; i++) {
            System.out.println(i+" ["+messages[i].getMessageNumber()+"] : "+messages[i].getSubject());
        }
    }

}
