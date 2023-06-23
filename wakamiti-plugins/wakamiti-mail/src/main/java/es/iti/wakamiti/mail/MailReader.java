package es.iti.wakamiti.mail;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.mail.Message;
import java.util.Arrays;
import java.util.Properties;

public class MailReader {
      public void processMessages(Message[] messages, Document xmlDoc) throws Exception {
        Element rootElement = xmlDoc.createElement("mails");
        xmlDoc.appendChild(rootElement);

        for (Message message : messages) {
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
            contentElement.appendChild(xmlDoc.createTextNode((String) message.getContent()));
            emailElement.appendChild(contentElement);

            rootElement.appendChild(emailElement);
        }

    }

    public Properties getMailProperties(String protocol, String host, String port, String user, String password) {
        Properties properties = new Properties();
        properties.setProperty("mail.server.protocol", protocol);
        properties.setProperty("mail.server.host", host);
        properties.setProperty("mail.server.port", port);
        properties.setProperty("mail.server.user", user);
        properties.setProperty("mail.server.password", password);
        return properties;
    }
}