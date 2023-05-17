Wakamiti File Uploader
======================

This plugin registers an event observer that reacts when an output file is written and 
attempts to upload it to a remote location using the FTP/FTPS protocol. More specifically, 
it reacts to the events of type `STANDARD_OUTPUT_FILE_WRITTEN`, `TEST_CASE_OUTPUT_FILE_WRITTEN`, and
`REPORT_OUTPUT_FILE_WRITTEN`.


Configuration
-------------

### `fileUploader.enable` : `true`|`false`

Enable / disable the event observer. Default value is `false`
   

### `fileUploader.host`
The host name or IP address to be used to upload the files. Optionally, it can include a specific
port in the form `hostname:port`

### `fileUploader.credentials.username`
The username to be used to stablish the FTP/FTPS connection

### `fileUploader.credentials.password`
The password to be used to stablish the FTP/FTPS connection

### `fileUploader.protocol` : `ftp` | `ftps`
The specific protocol to be used (`ftps` is recommended)

### `fileUploader.destinationDir`
The destination directory where the files should be uploaded within the remote location. It can include
placeholders like `%DATE%`, `%TIME%`, or `%execID%`


---

This configuration would be applied to every event type. However, they can be fine-tunned in case 
you require configuration variations according the even type. For example, the configuration 
below would use the same connection parameters but upload files to different directories:

```yaml
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