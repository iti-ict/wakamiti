# jExt

jExt is a simple library that allows you to define a *plug-in* architecture detecting the
Java classes that implements a specific interface. It mostly relies on the standard
[extension mechanism provided](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html)
by Java by means of the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
class, but it simplifies and enhances the usage.

The key concepts are:

- **Extension points** (interfaces annotated with `@ExtensionPoint`) which define the contract
- **Extensions** (classes annotated with `@Extension` and implementing the extension point class)
- **Extension managers** that are responsible for retrieve extensions for a specific extension point

What is the extra value then?

- Annotation processing of `@Extension` and `@ExtensionPoint` so the classes are
automatically registered as services by the compiler
- Capability of using multiple class loaders at the same time
- Explicitly declaring the extension point version that implements each extension, avoiding using wrong jar versions
- Custom control about creating new extension instances or reusing existing ones
- Fine extension filtering using whitelists and/or blacklists
- When extending an extension via inheritance, you may either override the parent class or use both superclass and
  subclass independently

---

## Prerequisites
- Java 11 or later


## Usage

### Dependency

#### Maven
Include the following within the `<dependencies>` section of your `pom.xml` file:
```xml
<dependency>
    <groupId>iti.commons</groupId>
    <artifactId>jext</artifactId>
    <version>1.1.0</version>
</dependency>
```


## License
```
Mozilla Public License 2.0

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.
```


## Contributing
TODO

## Authors
- Luis Iñesta Gelabert  |  :email: <linesta@iti.es> 