---
title: File steps
date: 2022-09-20
slug: /en/plugins/files
---

This plugin provides steps to operate on local files and directories during test execution.
It is useful in end-to-end scenarios where the system creates, modifies, copies, moves, or deletes files.

---
## Table of contents

---

## Install

Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:io-wakamiti-plugin:2.7.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>io-wakamiti-plugin</artifactId>
  <version>2.7.0</version>
</dependency>
```

## Options

### `files.timeout`
- Type: `long`
- Default: `60`

Maximum wait time in seconds for file wait steps.

Example:
```yaml
files:
  timeout: 120
```

### `files.enableCleanupUponCompletion`
- Type: `boolean`
- Default: `false`

If enabled, tracked file operations are automatically cleaned up at the end of execution.

Example:
```yaml
files:
  enableCleanupUponCompletion: true
```

### `files.links`
- Type: `string`

Defines symbolic links created during setup.
Format: `source=target`, separated by commas or semicolons.

Example:
```yaml
files:
  links: "./tmp/incoming=./runtime/incoming; ./tmp/out=./runtime/out"
```

## Steps

### Define timeout
```text copy=true
a file timeout of {value} seconds
```
Declarative way to set the configuration property [`files.timeout`](#filestimeout).

#### Parameters:
| Name    | Wakamiti type     | Description        |
|---------|-------------------|--------------------|
| `value` | `long` *required* | Timeout in seconds |

#### Examples:
```gherkin
Given a file timeout of 120 seconds
```


### Move file or directory to a directory
```text copy=true
the (file|directory) {src} is moved to directory {dest}
```
Moves a file or directory to a target directory.

#### Parameters:
| Name   | Wakamiti type     | Description                 |
|--------|-------------------|-----------------------------|
| `src`  | `file` *required* | Source file or directory    |
| `dest` | `file` *required* | Destination directory       |

#### Examples:
```gherkin
When the file 'tmp/report.txt' is moved to directory 'archive'
```


### Move file or directory to a file
```text copy=true
the (file|directory) {src} is moved to file {dest}
```
Moves a file or directory to a specific destination path.

#### Parameters:
| Name   | Wakamiti type     | Description                 |
|--------|-------------------|-----------------------------|
| `src`  | `file` *required* | Source file or directory    |
| `dest` | `file` *required* | Destination file path       |

#### Examples:
```gherkin
When the file 'tmp/report.txt' is moved to file 'archive/report-old.txt'
```


### Copy file or directory to a directory
```text copy=true
the (file|directory) {src} is copied to directory {dest}
```
Copies a file or directory to a target directory.

#### Parameters:
| Name   | Wakamiti type     | Description                 |
|--------|-------------------|-----------------------------|
| `src`  | `file` *required* | Source file or directory    |
| `dest` | `file` *required* | Destination directory       |

#### Examples:
```gherkin
When the file 'template.txt' is copied to directory 'out'
```


### Copy file or directory to a file
```text copy=true
the (file|directory) {src} is copied to file {dest}
```
Copies a file or directory to a specific destination path.

#### Parameters:
| Name   | Wakamiti type     | Description                 |
|--------|-------------------|-----------------------------|
| `src`  | `file` *required* | Source file or directory    |
| `dest` | `file` *required* | Destination file path       |

#### Examples:
```gherkin
When the file 'template.txt' is copied to file 'out/generated.txt'
```


### Delete file or directory
```text copy=true
the (file|directory) {file} is deleted
```
Deletes a file or directory.

#### Parameters:
| Name   | Wakamiti type     | Description              |
|--------|-------------------|--------------------------|
| `file` | `file` *required* | File or directory to delete |

#### Examples:
```gherkin
When the directory 'tmp/data' is deleted
```


### Wait for deletion
```text copy=true
the (file|directory) {file} deletion is awaited
```
Waits until a file or directory is deleted, up to the configured timeout.

#### Parameters:
| Name   | Wakamiti type     | Description                 |
|--------|-------------------|-----------------------------|
| `file` | `file` *required* | File or directory to watch  |

#### Examples:
```gherkin
When the file 'out/processing.lock' deletion is awaited
```


### Wait for modification
```text copy=true
the (file|directory) {file} modification is awaited
```
Waits until a file or directory is modified, up to the configured timeout.

#### Parameters:
| Name   | Wakamiti type     | Description                 |
|--------|-------------------|-----------------------------|
| `file` | `file` *required* | File or directory to watch  |

#### Examples:
```gherkin
When the file 'out/report.txt' modification is awaited
```


### Wait for creation
```text copy=true
the (file|directory) {file} creation is awaited
```
Waits until a file or directory is created, up to the configured timeout.

#### Parameters:
| Name   | Wakamiti type     | Description                 |
|--------|-------------------|-----------------------------|
| `file` | `file` *required* | File or directory to watch  |

#### Examples:
```gherkin
When the file 'out/report.txt' creation is awaited
```


### Check file exists
```text copy=true
the file {file} exists
```
Verifies that the file exists.

#### Parameters:
| Name   | Wakamiti type     | Description        |
|--------|-------------------|--------------------|
| `file` | `file` *required* | File path to check |

#### Examples:
```gherkin
Then the file 'out/report.txt' exists
```


### Check file does not exist
```text copy=true
the file {file} not exists
```
Verifies that the file does not exist.

#### Parameters:
| Name   | Wakamiti type     | Description        |
|--------|-------------------|--------------------|
| `file` | `file` *required* | File path to check |

#### Examples:
```gherkin
Then the file 'tmp/old-report.txt' not exists
```


### Check file text content
```text copy=true
the file {file} contains the following text:
    {data}
```
Verifies that the full file text matches the given in-document content.

#### Parameters:
| Name   | Wakamiti type         | Description              |
|--------|-----------------------|--------------------------|
| `file` | `file` *required*     | File path to check       |
| `data` | `document` *required* | Expected full text       |

#### Examples:
```gherkin
Then the file 'out/report.txt' contains the following text:
  """
  Status: OK
  """
```


### Check file content by ranges
```text copy=true
the file {file} contains the following data:
    {table}
```
Verifies fragments of the file content by character ranges.
The data table must include columns: `from`, `to`, `value`.

#### Parameters:
| Name    | Wakamiti type      | Description                                 |
|---------|--------------------|---------------------------------------------|
| `file`  | `file` *required*  | File path to check                          |
| `table` | `table` *required* | Ranges and expected values (`from`,`to`,`value`) |

#### Examples:
```gherkin
Then the file 'out/report.txt' contains the following data:
  | from | to | value  |
  | 0    | 6  | Status |
  | 8    | 10 | OK     |
```


### Check file length
```text copy=true
the file {file} has length of {chars}
```
Verifies that the file length in bytes is equal to the provided number.

#### Parameters:
| Name    | Wakamiti type        | Description                    |
|---------|----------------------|--------------------------------|
| `file`  | `file` *required*    | File path to check             |
| `chars` | `integer` *required* | Expected file length in bytes  |

#### Examples:
```gherkin
Then the file 'out/report.txt' has length of 128
```
