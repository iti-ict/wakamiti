This plugin fetches unread emails from the inbox and saves them in an XML file.

**Configuration**:
- [`mail.protocol`](#mailprotocol)
- [`mail.hostAndPort`](#mailhostandport)
- [`mail.address`](#mailaddress)
- [`mail.password`](#mailpassword)
- [`mail.createXml`](#mailcreatexml)

**Steps:**
- [Define the server protocol](#define-the-server-protocol)
- [Define host and server port](#define-host-and-server-port)
- [Define the mail address](#define-the-mail-address)
- [Define mail password](#define-email-password)
- [Create XML file](#create-xml-file)

## Configuration

---
###  `mail.protocol`
Sets the protocol used by the mail server.

Example:
```yaml
mail:
  protocol: imaps
```

### `mail.hostAndPort`
Sets the host and port used by the mail server.

Example:
```yaml
mail:
  host: imap.gmail.com
  port: 993
```

### `mail.address`
Set the mail address.

Example:
```yaml
mail:
  address: user@example.com
```

### `mail.password`
Set the mail password. If two-step verification is enabled, an application password must be set instead of the usual password. Depending on the type of host, this password will be generated in one way or another.

--------- OUTLOOK---------

Access to https://mysignins.microsoft.com/security-info

Choose a method → Application password

--------- GOOGLE ---------

Access to https://myaccount.google.com/

Search for "Application passwords".

Create a mail password

Example:
```yaml
mail:
  password: dknznxxxxxxxxxxx
```

### `mail.createXml`
It executes all the necessary functions to create the XML file with the data.

The functions are:
###### getSession()
<small>Returns the server configuration properties </small>.
###### connectToMailServer()
<small>Create the mail session and log in.</small>
###### openInboxFolder()
<small>Open the inbox in the mail service.</small>
###### searchUnreadMails()
<small>Returns unread emails.</small>
###### createXmlDocument()
<small>Creates a new XML file.</small>
###### saveXmlDocument()
<small>Inserts the information of the mails obtained in the XML.</small>

Example:
```yaml
mail:
  xmlName: unreadMails
```
## Steps
### Define the server protocol
Sets the protocol used by the mail server.

##### Example:
```gherkin
Given that the mail server protocol is imaps
```
### Define host and server port
Sets the host and port used by the mail server.
#### Example:
```gherkin
Given that the host and port of the mail server are imap.gmail.com and 993
```
### Define the mail address
Sets the mail address to be used by the server.
#### Example:
```gherkin
Given that the mail address is example@gmail.com
```
### Define mail password
Set the mail password.
#### Example:
```gherkin
Given that the mail password is fgbloedspotyuibd
```
### Create XML file
Create the XML file with the mail data.
#### Example:
```gherkin
Then the xml file is created
```
