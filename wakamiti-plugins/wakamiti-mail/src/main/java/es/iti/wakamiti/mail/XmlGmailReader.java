package es.iti.wakamiti.mail;

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

public class XmlGmailReader {
    public static void main(String[] args) throws NamingException {
        Properties properties = new Properties();
        properties.setProperty("mail.server.protocol", "imaps");
        properties.setProperty("mail.server.host", "imap.gmail.com");
        properties.setProperty("mail.server.port", "993");

        try {
            Session session = Session.getInstance(properties, null);

            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", "", "");

            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message[] unreadMessages = folder.search(unseenFlagTerm);

            int count = 0;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document xmlDoc = dBuilder.newDocument();
            Element rootElement = xmlDoc.createElement("mails");
            xmlDoc.appendChild(rootElement);

            for (Message message : unreadMessages) {
                Element emailElement = xmlDoc.createElement("mail");

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

                count++;
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xmlDoc);
            StreamResult result = new StreamResult(new File("emails.xml"));
            transformer.transform(source, result);

            System.out.println("Número de mensajes mostrados: " + count);

            folder.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XmlGmailReader() throws NamingException, MessagingException {
    }
}
