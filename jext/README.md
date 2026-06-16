# jExt

`jext` is the generic extension-loading library used by Wakamiti. It builds on Java `ServiceLoader`, adds annotation processing and exposes a higher-level API for discovering and filtering implementations of an extension point.

## What it provides

- `@ExtensionPoint` to declare an extension contract
- `@Extension` to describe concrete implementations
- compile-time registration through the bundled annotation processor
- version compatibility checks between extension points and implementations
- priority ordering, override handling and singleton/new-instance loading strategies
- lookup across one or more class loaders
- additional extension loaders discovered through `ServiceLoader`

## Requirements

- Java 11 or later

## Maven dependency

```xml
<dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>jext</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Minimal example

Define the extension point:

```java
@ExtensionPoint(version = "1.0")
public interface MessageFormatter {
    String format(String value);
}
```

Provide one or more implementations:

```java
@Extension(
    provider = "example",
    name = "plain",
    version = "1.0.0",
    priority = 100
)
public class PlainMessageFormatter implements MessageFormatter {
    @Override
    public String format(String value) {
        return value;
    }
}
```

Resolve them at runtime:

```java
ExtensionManager manager = new ExtensionManager();

MessageFormatter formatter = manager
        .getExtension(MessageFormatter.class)
        .orElseThrow();

List<MessageFormatter> allFormatters = manager
        .getExtensions(MessageFormatter.class)
        .toList();
```

## Notes

- `ExtensionManager#getExtension(...)` returns the highest-priority valid extension.
- `ExtensionManager#getExtensions(...)` returns all valid extensions ordered by priority.
- External loaders can contribute implementations as long as they expose `ExtensionLoader` through `META-INF/services`.

## License

```text
Mozilla Public License 2.0

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.
```
