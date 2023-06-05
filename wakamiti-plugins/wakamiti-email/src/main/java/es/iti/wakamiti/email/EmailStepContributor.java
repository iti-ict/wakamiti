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

import javax.mail.*;
import javax.mail.search.FlagTerm;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Properties;

@I18nResource("iti_wakamiti_wakamiti-email")
@Extension(provider = "es.iti.wakamiti", name = "email-steps")
public class EmailStepContributor implements StepContributor {
    private final EmailReader emailReader = new EmailReader();
    private String protocol;
    private String host;
    private String port;
    private String user;
    private String password;

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
        emailReader.processMessages(unreadMessages, xmlDoc);
            saveXmlDocument(xmlDoc);
            closeConnections(folder, store);
    }

    private Properties getMailProperties(String protocol, String host, String port, String user, String password) {
        Properties properties = new Properties();
        properties.setProperty("mail.server.protocol", protocol);
        properties.setProperty("mail.server.host", host);
        properties.setProperty("mail.server.port", port);
        properties.setProperty("mail.server.user", user);
        properties.setProperty("mail.server.password", password);
        return properties;
    }

    @Step(value = "mail.auth.session")
    private Session getSession(Properties properties) {
        return Session.getInstance(properties, null);
    }

    @Step(value = "mail.auth.connection")
    private Store connectToMailServer(Session session) throws MessagingException {
        Store store = session.getStore(protocol);
        store.connect(host, user, password);
        return store;
    }

    @Step(value = "mail.auth.openInboxFolder")
    private Folder openInboxFolder(Store store) throws MessagingException {
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        return folder;
    }

    @Step(value = "mail.auth.unreadMessages")
    private Message[] searchUnreadMessages(Folder folder) throws MessagingException {
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        return folder.search(unseenFlagTerm);
    }

    @Step(value = "mail.auth.createXml")
    private org.w3c.dom.Document createXmlDocument() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.newDocument();
    }

    @Step(value = "mail.auth.process")
    private void processMessages(Message[] messages, org.w3c.dom.Document xmlDoc) throws Exception {
        emailReader.processMessages(messages, xmlDoc);
    }

    @Step(value = "mail.auth.saveXml")
    private void saveXmlDocument(org.w3c.dom.Document xmlDoc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(xmlDoc);
        StreamResult result = new StreamResult(new File("emails.xml"));
        transformer.transform(source, result);
    }

    @Step(value = "mail.auth.close")
    private void closeConnections(Folder folder, Store store) throws MessagingException {
        folder.close(false);
        store.close();
    }


}