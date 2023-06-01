/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.email;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import org.w3c.dom.Element;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

@I18nResource("iti_wakamiti_wakamiti-email")
@Extension(provider = "es.iti.wakamiti", name = "email-steps")
public class EmailStepContributor implements StepContributor {
    private static String protocol;
    private static String host;
    private String port;
    private static String user;
    private static String password;

    @Step(value = "mail.server.protocol", args = "text")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Step(value = "mail.server.host", args = "text")
    public void setHost(String host) {
        this.host = host;
    }

    @Step(value = "mail.server.port", args = "text")
    public void setPort(String port) {
        this.port = port;
    }

    @Step(value = "mail.server.user", args = "text")
    public void setUser(String user) {
        this.user = user;
    }

    @Step(value = "mail.server.password", args = "text")
    public void setPassword(String password) {
        this.password = password;
    }

    public void leerCorreo() throws Exception {
            Properties properties = getMailProperties(this.protocol, this.host, this.port, this.user, this.password);
            Session session = getSession(properties);
            Store store = connectToMailServer(session);
            Folder folder = openInboxFolder(store);
            Message[] unreadMessages = searchUnreadMessages(folder);
            org.w3c.dom.Document xmlDoc = createXmlDocument();
            processMessages(unreadMessages, xmlDoc);
            saveXmlDocument(xmlDoc);
            closeConnections(folder, store);
    }


    private static Properties getMailProperties(String protocol, String host, String port, String user, String password) {
        Properties properties = new Properties();
        properties.setProperty("mail.server.protocol", protocol);
        properties.setProperty("mail.server.host", host);
        properties.setProperty("mail.server.port", port);
        properties.setProperty("mail.server.user", user);
        properties.setProperty("mail.server.password", password);
        return properties;
    }

    private static Session getSession(Properties properties) {
        return Session.getInstance(properties, null);
    }

    private static Store connectToMailServer(Session session) throws MessagingException {
        Store store = session.getStore(protocol);
        store.connect(host, user, password);
        return store;
    }

    private static Folder openInboxFolder(Store store) throws MessagingException {
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        return folder;
    }

    private static Message[] searchUnreadMessages(Folder folder) throws MessagingException {
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        return folder.search(unseenFlagTerm);
    }

    private static org.w3c.dom.Document createXmlDocument() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.newDocument();
    }

    private static void processMessages(Message[] messages, org.w3c.dom.Document xmlDoc) throws Exception {
        Element rootElement = xmlDoc.createElement("emails");
        xmlDoc.appendChild(rootElement);

        for (Message message : messages) {
            Element emailElement = xmlDoc.createElement("email");

            Element dateElement = xmlDoc.createElement("date");
            dateElement.appendChild(xmlDoc.createTextNode(message.getSentDate().toString()));
            emailElement.appendChild(dateElement);

            Element fromElement = xmlDoc.createElement("from");
            fromElement.appendChild(xmlDoc.createTextNode(message.getFrom()[0].toString()));
            emailElement.appendChild(fromElement);

            Element toElement = xmlDoc.createElement("to");
            toElement.appendChild(xmlDoc.createTextNode(Arrays.toString(message.getRecipients(Message.RecipientType.TO))));
            emailElement.appendChild(toElement);

            Element subjectElement = xmlDoc.createElement("subject");
            subjectElement.appendChild(xmlDoc.createTextNode(message.getSubject()));
            emailElement.appendChild(subjectElement);

            Element contentElement = xmlDoc.createElement("content");

            if (message.getContent() instanceof Multipart) {
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        String fileName = bodyPart.getFileName();
                        String downloadsDir = System.getProperty("user.home") + "/Downloads/";
                        String filePath = downloadsDir + fileName;
                        InputStream inputStream = bodyPart.getInputStream();
                        OutputStream outputStream = new FileOutputStream(new File(filePath));

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.close();
                        inputStream.close();

                        Element attachmentElement = xmlDoc.createElement("attachment");
                        attachmentElement.appendChild(xmlDoc.createTextNode(fileName));
                        emailElement.appendChild(attachmentElement);
                    }
                }
            }

            Object emailContent = message.getContent();
            if (emailContent instanceof Multipart) {
                Multipart multipart = (Multipart) emailContent;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (bodyPart.isMimeType("text/html")) {
                        String html = (String) bodyPart.getContent();

                        Element textElement = xmlDoc.createElement("text");
                        textElement.appendChild(xmlDoc.createTextNode(html));
                        contentElement.appendChild(textElement);
                    }
                }
            } else if (emailContent instanceof String) {
                Element textElement = xmlDoc.createElement("text");
                textElement.appendChild(xmlDoc.createTextNode(emailContent.toString()));
                contentElement.appendChild(textElement);
            }

            emailElement.appendChild(contentElement);
            rootElement.appendChild(emailElement);

        }

    }


    private static void saveXmlDocument(org.w3c.dom.Document xmlDoc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(xmlDoc);
        StreamResult result = new StreamResult(new File("emails.xml"));
        transformer.transform(source, result);
    }

    private static void closeConnections(Folder folder, Store store) throws MessagingException {
        folder.close(false);
        store.close();
    }


}