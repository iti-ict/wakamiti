---
title: Appium
date: 2023-06-09
slug: /en/plugins/appium
---


This plugin allows Wakamiti to write scenarios interacting with an [Appium](http://appium.io/docs/en/2.0/) server. 
Appium is an open source project designed to help automate UI testing on various platforms, including mobile.

A running Appium server and an emulated virtual device are required to use this plugin.

> **DISCLAIMER**
> 
> This plugin is currently more of a proof of concept than a fully functional plugin, and steps and configuration may 
> vary in future versions.


---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:appium-wakamiti-plugin:2.5.0
```

```text tabs=coord name=maven copy=true
<dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>appium-wakamiti-plugin</artifactId>
    <version>2.5.0</version>
</dependency>
```


## Options


> Each capability to be passed to the web driver can be specified using the appropriate key in the format 
> `appium.capabilities.xxxxxx`. This document describes the most relevant options, but for a complete list of available
> properties, see the [W3C WebDriver draft](https://w3c.github.io/webdriver/#capabilities).


### `appium.url`
- Type: `string` *required*

The URL of the Appium server

Example:
```yaml
appium:
  url: http://127.0.0.1:4723/wd/hub
```


### `appium.capabilities.app`
- Type: `file` *required*

The full path of the packaged app to test

Example:
```yaml
appium:
  capabilities:
    app: ApiDemos-debug.apk
```


### `appium.capabilities.platformName`
- Type: `string` *required*

The mobile platform that would be tested 

Example:
```yaml
appium:
  capabilities:
    platformName: Android
```


### `appium.capabilities.platformVersion`
- Type: `integer` *required*

The version of the platform that would be tested

Example:
```yaml
appium:
  capabilities:
    platformVersion: 11
```


### `appium.capabilities.appPackage`
- Type: `string` *required*

The name of the package that contains the application to be tested

Example:
```yaml
appium:
  capabilities:
    appPackage: io.appium.android.apis
```


### `appium.capabilities.appActivity`
- Type: `string` *required*

The name of the activity to be tested

Example:
```yaml
appium:
  capabilities:
    appActivity: '.view.TextFields'
```


## Steps


### Select a UI element by its ID
```text copy=true
the UI element with ID {text}
```
Selects an element that would be the subject of the following steps.

#### Parameters
| Name   | Wakamiti type     | Description    |
|--------|-------------------|----------------|
| `text` | `text` *required* | The element ID |

#### Examples:
```gherkin
Given the UI element with ID '3423423'
```


### Select a UI element by its type
```text copy=true
the UI element with type {text}
```
Selects an element that would be the subject of the following steps.

#### Parameters
| Name   | Wakamiti type     | Description      |
|--------|-------------------|------------------|
| `text` | `text` *required* | The element type |

#### Examples:
```gherkin
Given the UI element with type 'android.widget.EditText'
```


### Select a UI element by its path
```text copy=true
the UI element with path {text}
```
Selects an element that would be the subject of the following steps.

#### Parameters
| Name   | Wakamiti type     | Description      |
|--------|-------------------|------------------|
| `text` | `text` *required* | The element path |

#### Examples:
```gherkin
Given the UI element with path 'main.form.name'
```


### Type a text on an element
```text copy=true
the text {text} is typed on that element
```
Emulates the action of typing a text when an element is selected.

#### Parameters
| Name   | Wakamiti type     | Description         |
|--------|-------------------|---------------------|
| `text` | `text` *required* | The element content |

#### Examples:
```gherkin
When the text 'John' is typed on that element
```


### Tap on an element
```text copy=true
a tap is done over that element
```
Emulates the action of tapping on the selected element.

#### Examples:
```gherkin
When a tap is done over that element
```


### Double-tap on an element
```text copy=true
a double-tap is done over that element
```
Emulates the action of double-tapping on the selected element.

#### Examples:
```gherkin
When a double-tap is done over that element
```


### Validate the text of an element
```text copy=true
that element contains the value {text}
```
Asserts that the previously selected element contains a certain text.

#### Examples:
```gherkin
Then that element contains the value 'Accepted'
```


### Validate an element is enabled
```text copy=true
that element is enabled
```
Asserts that the previously selected element is currently enabled.

#### Examples:
```gherkin
Then that element is enabled
```


### Validate an element is disabled
```text copy=true
that element is disabled
```
Asserts that the previously selected element is currently disabled.

#### Examples:
```gherkin
Then that element is disabled
```


### Validate an element is displayed
```text copy=true
that element is displayed
```
Asserts that the previously selected element is displayed on screen.

#### Examples:
```gherkin
Then that element is displayed
```


### Validate an element is not displayed
```text copy=true
that element is not displayed
```
Asserts that the previously selected element is not displayed on screen.

#### Examples:
```gherkin
Then that element is not displayed
```


### Emulate an incoming call
```text copy=true
an incoming call with number {text} is received
```
Emulates an incoming call from a specific phone number. Only available if the tested application runs on an emulated 
device.

#### Parameters
| Name   | Wakamiti type     | Description      |
|--------|-------------------|------------------|
| `text` | `text` *required* | The phone number |

#### Examples:
```gherkin
When an incoming call with number '555-4324-432' is received
```


### Accepts an incoming call
```text copy=true
the incoming call is accepted
```
Accepts the current incoming call. 

#### Examples:
```gherkin
When the incoming call is accepted
```


### Rejects an incoming call
```text copy=true
the incoming call is rejected
```
Rejects the current incoming call.

#### Examples:
```gherkin
When the incoming call is rejected
```


### Ends the current call
```text copy=true
the call is ended
```
Ends (hangs up) the current call.

#### Examples:
```gherkin
When the call is ended
```


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

4. Install Appium and Appium-doctor
```text copy=true
   npm install -g appium
   npm install @appium/doctor --location=global
```

5. Check the installation
```text copy=true
appium-doctor --android
```
All should be OK, otherwise check the red messages

6. Start the Appium server
```text copy=true
appium
```
Default port is 4723

Download a demo APK from:
https://github.com/appium/appium/raw/1.x/sample-code/apps/ApiDemos-debug.apk

7. Create an Android Virtual Device
```text copy=true
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd --force --name Nexus6P --abi google_apis_playstore/x86 --package 'system-images;android-30;google_apis_playstore;x86' --device "Nexus 6P"
```

8. Start the emulator
```text copy=true
$ANDROID_HOME/emulator/emulator -avd Nexus6P
```

In case the emulator fails due to a previous emulation process in a zombie state, it can be cleared using
```text copy=true
$ANDROID_HOME/platform-tools/adb kill-server
```


Ideally, we would like to be able to do all this semi-automatically from the APK, but for now, we have to:
- Install Android Studio
- Create a virtual device (AVD) e.g., Pixel 2 API 30 with Android 11
- Launch the AVD with the play button



[android-sdk]: https://iti-ict.github.io/wakamiti/android-sdk.png
