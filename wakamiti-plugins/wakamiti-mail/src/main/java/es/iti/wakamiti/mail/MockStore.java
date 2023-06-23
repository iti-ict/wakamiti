package es.iti.wakamiti.mail;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.URLName;

public interface MockStore {
    Folder[] getSharedNamespaces(String user) throws MessagingException;

    Folder getDefaultFolderNewStyle() throws MessagingException;

    Folder getFolder(URLName arg0, String arg1) throws MessagingException;
}
