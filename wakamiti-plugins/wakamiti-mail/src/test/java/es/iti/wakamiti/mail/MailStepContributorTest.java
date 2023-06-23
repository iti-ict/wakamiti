package es.iti.wakamiti.mail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class MailStepContributorTest {
    private MockMailStepContributor mailStepContributor;
    private Store store;
    private static Message[] messages;

    @Before
    public void setUp() throws MessagingException {
        mailStepContributor = new MockMailStepContributor();

        Properties properties = new Properties();
        properties.setProperty("mail.protocol", "mock");
        properties.setProperty("mail.host", "mock.host");
        properties.setProperty("mail.port", "1234");
        properties.setProperty("mail.user", "mock.user");
        properties.setProperty("mail.password", "mock.password");

        store = mailStepContributor.connectToMailServer(properties);

        mailStepContributor.openInboxFolder(store);
        mailStepContributor.searchUnreadMails(store.getFolder("INBOX"));
    }

    @Test
    public void testMailSteps() throws Exception {

        messages = createMockMessages();
        mailStepContributor.setMessages(messages);

        Assert.assertEquals("Test Subject", mailStepContributor.getSubject());
        Assert.assertEquals("sender@example.com", mailStepContributor.getSender());
        Assert.assertEquals("recipient@example.com", mailStepContributor.getRecipient());
        Assert.assertEquals("Test Body", mailStepContributor.getBody());
        String attachments = mailStepContributor.getAttachment();
        Assert.assertEquals("attachment1.txt, attachment2.pdf", attachments);

        mailStepContributor.markAsRead();

        Assert.assertTrue(messages[0].isSet(Flags.Flag.SEEN));
        Assert.assertTrue(messages[1].isSet(Flags.Flag.SEEN));
    }

    private Message[] createMockMessages() throws MessagingException {
        Address sender1 = new InternetAddress("sender@example.com");
        Address recipient1 = new InternetAddress("recipient@example.com");

        Address sender2 = new InternetAddress("another-sender@example.com");
        Address recipient2 = new InternetAddress("another-recipient@example.com");

        Message message1 = new MimeMessage((Session) null);
        message1.setSubject("Test Subject");
        message1.setFrom(sender1);
        message1.setRecipient(Message.RecipientType.TO, recipient1);
        message1.setContent("Test Body", "text/plain");
        message1.setFlag(Flags.Flag.SEEN, false);

        MimeBodyPart attachment1 = new MimeBodyPart();
        String attachmentFileName1 = "attachment1.txt";
        attachment1.setFileName(attachmentFileName1);

        String attachmentContent1 = "Test Body";
        DataSource dataSource1 = new ByteArrayDataSource(attachmentContent1.getBytes(), "text/plain");
        attachment1.setDataHandler(new DataHandler(dataSource1));

        Multipart multipart1 = new MimeMultipart();
        multipart1.addBodyPart(attachment1);
        message1.setContent(multipart1);

        Message message2 = new MimeMessage((Session) null);
        message2.setSubject("Another Subject");
        message2.setFrom(sender2);
        message2.setRecipient(Message.RecipientType.TO, recipient2);
        message2.setContent("Another Body", "text/plain");
        message2.setFlag(Flags.Flag.SEEN, false);

        MimeBodyPart attachment2 = new MimeBodyPart();
        String attachmentFileName2 = "attachment2.pdf";
        attachment2.setFileName(attachmentFileName2);

        String attachmentContent2 = "This is the content of attachment2";
        DataSource dataSource2 = new ByteArrayDataSource(attachmentContent2.getBytes(), "application/pdf");
        attachment2.setDataHandler(new DataHandler(dataSource2));

        Multipart multipart2 = new MimeMultipart();
        multipart2.addBodyPart(attachment2);
        message2.setContent(multipart2);

        return new Message[]{message1, message2};
    }

    @After
    public void tearDown() throws MessagingException {
        mailStepContributor.closeConnections();
    }


}
