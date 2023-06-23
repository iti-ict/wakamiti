Este plugin obtiene los correos electrónicos no leídos de la bandeja de entrada y los guarda en un archivo XML.

**Configuración**:
- [`mail.protocol`](#mailprotocol)
- [`mail.hostAndPort`](#mailhostandport)
- [`mail.address`](#mailaddress)
- [`mail.password`](#mailpassword)
- [`mail.createXml`](#mailcreatexml)

**Pasos:**
- [Definir el protocolo del servidor](#definir-el-protocolo-del-servidor)
- [Definir host y puerto del servidor](#definir-host-y-puerto-del-servidor)
- [Definir la direccion de correo](#definir-la-direccion-de-correo)
- [Definir password del correo](#definir-password-del-correo)
- [Crear archivo XML](#crear-archivo-xml)

## Configuración

---
###  `mail.protocol`
Establece el protocolo que utiliza el servidor de correo.

Ejemplo:
```yaml
email:
  protocol: imaps
```

### `mail.hostAndPort`
Establece el host y puerto que utiliza el servidor de correo.

Ejemplo:
```yaml
mail:
  host: imap.gmail.com
  port: 993
```

### `mail.address`
Establece la dirección de correo.

Ejemplo:
```yaml
mail:
  address: user@example.com
```

### `mail.password`
Estable la contraseña de correo. En caso de tener activada la verificación en dos pasos se debe poner una contraseña de aplicación en lugar de la contraseña habitual. Dependiendo del tipo de host, esta clave se generará de una manera u otra.

--------- OUTLOOK---------

Acceder a https://mysignins.microsoft.com/security-info

Elegir un método → Contraseña de aplicación


--------- GOOGLE ---------

Acceder a https://myaccount.google.com/

Buscar "Contraseñas de aplicaciones"

Crear una contraseña tipo Correo


Ejemplo:
```yaml
mail:
  password: dknznxxxxxxxxxxx
```

### `mail.createXml`
Ejecuta todas las funciones necesarias para crear el archivo XML con los datos.

Las funciones son:
###### getSession()
<small>Devuelve las propiedades de configuración del servidor.</small>
###### connectToMailServer()
<small>Crea la sesión de correo electrónico y se conecta.</small>
###### openInboxFolder()
<small>Abre la bandeja de entrada en el servicio de correo.</small>
###### searchUnreadMails()
<small>Devuelve los correos no leídos.</small>
###### createXmlDocument()
<small>Crea un nuevo archivo XML.</small>
###### saveXmlDocument()
<small>Inserta la información de los correos obtenidos en el XML.</small>

Ejemplo:
```yaml
mail:
  xmlName: unreadMails
```
## Pasos
### Definir el protocolo del servidor
Establece el protocolo que utiliza el servidor de correo.

##### Ejemplo:
```gherkin
Dado que el protocolo del servidor de correo electrónico es imaps
```
### Definir host y puerto del servidor
Establece el host y puerto que utiliza el servidor de correo.
#### Ejemplo:
```gherkin
Dado que el host y el puerto del servidor de correo electrónico son imap.gmail.com y 993
```
### Definir la direccion de correo
Establece la dirección de correo que utilizará el servidor.
#### Ejemplo:
```gherkin
Dado que la dirección de correo electrónico es example@gmail.com
```
### Definir password del correo
Establece la contraseña del correo.
#### Ejemplo:
```gherkin
Dado que la contraseña del correo electrónico es fgbloedspotyuibd
```
### Crear archivo XML
Crear el archivo XML con los datos del correo.
#### Ejemplo:
```gherkin
Entonces se crea el archivo xml
```
