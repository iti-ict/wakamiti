---
title: Appium
date: 2023-06-09
slug: /en/plugins/appium
---

This plugin allows Wakamiti to write scenarios that interact with an 
[Appium](http://appium.io/docs/en/2.0/) server. Appium is an open-source project 
designed to facilitate UI automation of many app platforms, including mobile apps.

The usage of this plugin requires that an Appium server is up and running, as well as an
emulated virtual device. Below, there is an example of how to set up a testing environment
for this.

> **DISCLAIMER**
> 
> In its current state, this plugin is mostly a proof of concept rather than a fully 
> functional plugin, and the provided steps and configuration may vary in future 
> versions.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:appium-wakamiti-plugin:2.2.3
```

```text tabs=coord name=maven copy=true
<dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>appium-wakamiti-plugin</artifactId>
    <version>2.2.3</version>
</dependency>
```

---
## Table of content

---

---
## Configuration

> You can specify any capability to be passed to the Web Driver using the proper property key in
> the form ```appium.capabilities.xxxxxx```. In this document, the most relevant options are described,
> but for an exhaustive list of the available properties, please check the
> [W3C WebDriver draft](https://w3c.github.io/webdriver/#capabilities).

<br /><br />

### `appium.url` 
The URL of the Appium server

Example:
```yaml
appium:
  url: http://127.0.0.1:4723/wd/hub
```

<br /><br />

### `appium.capabilities.app`
The full path of the packaged app to test

Example:
```yaml
appium:
  capabilities:
    app: ApiDemos-debug.apk
```

<br /><br />

### `appium.capabilities.platformName`
The mobile platform that would be tested 

Example:
```yaml
appium:
  capabilities:
    platformName: Android
```

<br /><br />

### `appium.capabilities.platformVersion`
The version of the platform that would be tested

Example:
```yaml
appium:
  capabilities:
    platformVersion: 11
```

<br /><br />

### `appium.capabilities.appPackage`
The name of the package that contains the application to be tested

Example:
```yaml
appium:
  capabilities:
    appPackage: io.appium.android.apis
```

<br /><br />

### `appium.capabilities.appActivity`
The name of the activity to be tested

Example:
```yaml
appium:
  capabilities:
    appActivity: '.view.TextFields'
```

---
## Steps



### Select a UI element by its ID
```text copy=true
the UI element with ID {text}
```
Select an element that would be the subject of the following steps.

#### Examples:
```gherkin
  Given the UI element with ID '3423423'
```

<br /><br />

### Select a UI element by its type
```text copy=true
the UI element with type {text}
```
Select an element that would be the subject of the following steps.

#### Examples:
```gherkin
  Given the UI element with type 'android.widget.EditText'
```

<br /><br />

### Select a UI element by its path
```text copy=true
the UI element with path {text}
```
Select an element that would be the subject of the following steps.

#### Examples:
```gherkin
  Given the UI element with path 'main.form.name'
```

<br /><br />

### Type a text on an element
```text copy=true
the text {text} is typed on that element
```
Emulate the action of typing a text when an element is selected.

#### Examples:
```gherkin
  When the text 'John' is typed on that element
```

<br /><br />

### Tap on an element
```text copy=true
a tap is done over that element
```
Emulate the action of tapping on the selected element.

#### Examples:
```gherkin
  When a tap is done over that element
```

<br /><br />

### Double-tap on an element
```text copy=true
a double-tap is done over that element
```
Emulate the action of double-tapping on the selected element.

#### Examples:
```gherkin
  When a double-tap is done over that element
```

<br /><br />

### Validate the text of an element
```text copy=true
that element contains the value {text}
```
Assert that the previously selected element contains a certain text.

#### Examples:
```gherkin
  Then that element contains the value 'Accepted'
```

<br /><br />

### Validate an element is enabled
```text copy=true
that element is enabled
```
Assert that the previously selected element is currently enabled.

#### Examples:
```gherkin
  Then that element is enabled
```

<br /><br />

### Validate an element is disabled
```text copy=true
that element is disabled
```
Assert that the previously selected element is currently disabled.

#### Examples:
```gherkin
  Then that element is disabled
```

<br /><br />

### Validate an element is displayed
```text copy=true
that element is displayed
```
Assert that the previously selected element is displayed on screen.

#### Examples:
```gherkin
  Then that element is displayed
```

<br /><br />

### Validate an element is not displayed
```text copy=true
that element is not displayed
```
Assert that the previously selected element is not displayed on screen.

#### Examples:
```gherkin
  Then that element is not displayed
```

<br /><br />

### Emulate an incoming call
```text copy=true
an incoming call with number {text} is received
```
Emulate an incoming call from a specific phone number. Only available if the tested application 
runs on a emulated device.

#### Examples:
```gherkin
  When an incoming call with number '555-4324-432' is received
```

<br /><br />

### Accepts an incoming call
```text copy=true
the incoming call is accepted
```
Accepts the current incoming call. 

#### Examples:
```gherkin
  When the incoming call is accepted
```

<br /><br />

### Rejects an incoming call
```text copy=true
the incoming call is rejected
```
Rejects the current incoming call.

#### Examples:
```gherkin
  When the incoming call is rejected
```

<br /><br />

### Ends the current call
```text copy=true
the call is ended
```
Ends (hangs up) the current call.

#### Examples:
```gherkin
  When the call is ended
```



---
## Usage


1. Install Android SDK
```text copy=true
  sudo apt install android-sdk
```


2. Add the environment variable ANDROID_HOME
```
  (p.ej. $HOME/Android/Sdk )
```

3. Install cmdline-tools

![android-sdk][android-sdk]

5. Install Appium and Appium-doctor
```text copy=true
   npm install -g appium
   npm install @appium/doctor --location=global
```

4. Check the installation
```text copy=true
appium-doctor --android
```
All should be OK, otherwise check the red messages

5. Start the Appium server
```text copy=true
appium
```
Default port is 4723

Download a demo APK from:
https://github.com/appium/appium/raw/1.x/sample-code/apps/ApiDemos-debug.apk

6. Create an Android Virtual Device
```text copy=true
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd --force --name Nexus6P --abi google_apis_playstore/x86 --package 'system-images;android-30;google_apis_playstore;x86' --device "Nexus 6P"```
```

7. Start the emulator
```text copy=true
$ANDROID_HOME/emulator/emulator -avd Nexus6P
```

In case the emulator fails due to a previous emulation process in a zombie state, it can be 
cleared using
```text copy=true
$ANDROID_HOME/platform-tools/adb kill-server
```

[android-sdk]: https://iti-ict.github.io/wakamiti/android-sdk.png