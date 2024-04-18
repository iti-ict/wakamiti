---
title: File Uploader
date: 2023-05-29
slug: /en/plugins/fileuploader
---


This plugin registers an event observer that reacts when an output file is written and
attempts to upload it to a remote location using the FTP/FTPS protocol. More specifically,
it reacts to the events of type `STANDARD_OUTPUT_FILE_WRITTEN`, `TEST_CASE_OUTPUT_FILE_WRITTEN`, and
`REPORT_OUTPUT_FILE_WRITTEN`.


---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:file-uploader-wakamiti-plugin:2.6.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>file-uploader-wakamiti-plugin</artifactId>
  <version>2.6.0</version>
</dependency>
```


## Options


### `fileUploader.enabled` 
- Type: `boolean`
- Default: `true`

Indicates whether the plugin is activated.

Example:
```yaml
fileUploader:
  enabled: "false"
```


### `fileUploader.host`
- Type: `string` *required*

The name or IP address of the machine to which the files are to be uploaded. Optionally, you can include a port number, 
in the form `hostname:port`.

Example:
```yaml
fileUploader:
  host: 172.0.0.1:22
```


### `fileUploader.credentials.username`
- Type: `string` *required*

The username used to establish the connection.

Example:
```yaml
fileUploader:
  credentials: 
    username: test
```


### `fileUploader.credentials.password`
- Type: `string` *required*

The password used to establish the connection.

Example:
```yaml
fileUploader:
  credentials:
    password: test
```


### `fileUploader.protocol`
- Type: `string` 
- Default: `ftps`

The specific protocol to be used. Possible values are:
- `ftp`
- `ftps`
- `sftp`

Example:
```yaml
fileUploader:
  protocol: sftp
```


### `fileUploader.destinationDir`
- Type: `file` *required*

The destination directory where the files should be uploaded within the remote location. It can include
placeholders like `%DATE%`, `%TIME%`, or `%execID%`

Example:
```yaml
fileUploader:
  destinationDir: /home/test/file-%DATE%.txt
```


### `fileUploader.identity`
- Type: `file` 

Path of the identity file used to authentication. 

Example:
```yaml
fileUploader:
  identity: /.ssh/identity.ppk
```


## Usage

This global setting applies to all received event types. However, there is the possibility to set one or more properties 
with specific values depending on the type of event. For example, the following configuration would use the same 
connection parameters but upload files to different directories depending on the type of event:

```yaml copy=true
fileUploader:
  host: 192.168.1.40
  protocol: ftps
  credentials:
    username: test
    password: testpwd
  standardOutputs:
    destinationDir: data/results
  reportOutputs:
    destinationDir: data/reports
  testCaseOutputs:
    destinationDir: data/tests/%DATE%%TIME%
```