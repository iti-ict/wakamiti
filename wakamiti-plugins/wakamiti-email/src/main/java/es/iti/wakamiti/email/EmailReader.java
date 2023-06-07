package es.iti.wakamiti.email;

import es.iti.wakamiti.api.annotations.Step;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class EmailReader {
    public EmailReader() {
    }

    void processMessages(Message[] messages, Document xmlDoc) throws Exception {
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
}