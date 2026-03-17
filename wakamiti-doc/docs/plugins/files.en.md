---
title: File steps
date: 2022-09-20
slug: /en/plugins/files
---

This plugin provides steps to operate on local files and directories during test execution. It is useful for end-to-end
flows where the system creates, modifies, or removes files.

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

Maximum wait time (in seconds) used by file wait steps.

Example:
```yaml
files:
  timeout: 120
```

### `files.enableCleanupUponCompletion`
- Type: `boolean`
- Default: `false`

If enabled, file operations tracked by the plugin are cleaned up at the end of execution.

Example:
```yaml
files:
  enableCleanupUponCompletion: true
```

### `files.links`
- Type: `string`

Defines symbolic links created during setup. Format: `source=target`, separated by commas or semicolons.

Example:
```yaml
files:
  links: "./tmp/incoming=./runtime/incoming; ./tmp/out=./runtime/out"
```

## Main step groups

### File actions

```text copy=true
the (file|directory) {src} is moved to directory {dest}
the (file|directory) {src} is moved to file {dest}
the (file|directory) {src} is copied to directory {dest}
the (file|directory) {src} is copied to file {dest}
the (file|directory) {file} is deleted
```

### Wait operations

```text copy=true
the (file|directory) {file} deletion is awaited
the (file|directory) {file} modification is awaited
the (file|directory) {file} creation is awaited
```

### Assertions

```text copy=true
the file {file} exists
the file {file} not exists
the file {file} contains the following text:
the file {file} contains the following data:
the file {file} has length of {chars}
```

## Usage example

```gherkin
Scenario: Generated report is available
  Given a file timeout of 120 seconds
  When the file 'out/report.txt' creation is awaited
  Then the file 'out/report.txt' exists
```
