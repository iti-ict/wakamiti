package es.iti.wakamiti.email;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Properties;

import static org.junit.Assert.*;

public class EmailStepContributorTest {

    private static final String PROTOCOL = "imaps";
    private static final String HOST = "imap.gmail.com";
    private static final String PORT = "993";
    private static final String USER = "";
    private static final String PASSWORD = "";

    @Test
    public void testLeerCorreo() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("mail.server.protocol", PROTOCOL);
        properties.setProperty("mail.server.host", HOST);
        properties.setProperty("mail.server.port", PORT);
        properties.setProperty("mail.server.user", USER);
        properties.setProperty("mail.server.password", PASSWORD);

        Session session = Session.getInstance(properties, null);

        Store store = session.getStore(PROTOCOL);
        store.connect(HOST, USER, PASSWORD);

        javax.mail.Folder folder = store.getFolder("INBOX");
        folder.open(javax.mail.Folder.READ_WRITE);

        SearchTerm searchCriteria = new FlagTerm(new javax.mail.Flags(javax.mail.Flags.Flag.SEEN), false);
        Message[] unreadMessages = folder.search(searchCriteria);

        assertNotNull(unreadMessages);

        EmailStepContributor emailStepContributor = new EmailStepContributor();
        emailStepContributor.setProtocol(PROTOCOL);
        emailStepContributor.setHost(HOST);
        emailStepContributor.setPort(PORT);
        emailStepContributor.setUser(USER);
        emailStepContributor.setPassword(PASSWORD);
        emailStepContributor.leerCorreo();

        File xmlFile = new File("emails.xml");
        assertTrue(xmlFile.exists());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document xmlDoc = dBuilder.parse(xmlFile);
        xmlDoc.getDocumentElement().normalize();

        Element rootElement = xmlDoc.getDocumentElement();
        assertEquals("emails", rootElement.getNodeName());

        NodeList emailNodes = rootElement.getElementsByTagName("email");
        assertNotNull(emailNodes);
        assertFalse(emailNodes.getLength() == 0);

        for (int i = 0; i < emailNodes.getLength(); i++) {
            Element emailElement = (Element) emailNodes.item(i);

            assertNotNull(String.valueOf(emailElement.getElementsByTagName("date").item(0)));
            assertNotNull(String.valueOf(emailElement.getElementsByTagName("from").item(0)));
            assertNotNull(String.valueOf(emailElement.getElementsByTagName("to").item(0)));
            assertNotNull(String.valueOf(emailElement.getElementsByTagName("subject").item(0)));
            assertNotNull(String.valueOf(emailElement.getElementsByTagName("content").item(0)));
            assertNotNull(String.valueOf(emailElement.getElementsByTagName("text").item(0)));

        }

        folder.close(false);
        store.close();
    }
}
