Configurer
================================================================================
> A multi-purpose, immutable configuration interface

This library provides a simple interface in order to load and consume *configurations*,
which are mainly a set of valued properties that can be parsed to specific Java types.
The primary focus of the library is **null-safety**, **immutability**, and **fluency**.

The `Configuration` class is immutable in order to ensure the values are not modified
by any process, but it can build derived configurations. Also, when a property is not defined,
the `get` and `getList` methods return an empty `Optional` and an empty `List`, respectively,
instead of `null`.

There are a wide range of builder methods to get configurations from different sources, such as:
- OS environment variables
- Java system properties
- Java `.properties` files
- JSON files
- XAML files
- `Map` and `Properties` objects
- *In-code* pairs of <key, value>

The internal implementation is mainly a wrapper around [Apache Commons Configuration][1],
but it may change at any point.


Table of content
-----------------------------------------------------------------------------------------
- [Usage](#usage)
- [Requirements](#requirements)
- [Contributing](#contributing)
- [Authors](#authors)
- [License](#license)



Usage
-----------------------------------------------------------------------------------------

### Loading configurations

In order to obtain a configuration, simply use one of the static methods in `Configuration`:

```java
Configuration conf = Configuration.fromPath(Path.of("myConfig.yaml"));
```

Two configurations can be merged, using one of them as base:

```java
Configuration confA = Configuration.fromEnvironment();
Configuration confB = Configuration.fromPath(Path.of("myConfig.yaml"));
Configuration confC = confA.append(confB);
```
or
```java
Configuration conf = Configuration
  .fromEnvironment()
  .appendFromPath(Path.of("myConnfig.yaml"));
```

In addition, you can annotate any class and use it as a configuration source:

```java
@AnnotatedConfiguration(properties={
  @Property(key="property.a", value="A"),
  @Property(key="property.b", value="B")
})
class MyConfigClass { }
```
```java
Configuration conf = Configuration.fromAnnotation(MyConfigClass.class);
```

### Maven dependency
```xml
<dependency>
   <groupId>immutable-configurer</groupId>
   <artifactId>immutable-configurer</artifactId>
   <version>1.0.0</version>
</dependency>
```


[[back to top](#configurer)]

Requirements
-----------------------------------------------------------------------------------------
- Java 11 or newer.

[[back to top](#configurer)]

## Contributing

## Authors

- Luis Iñesta Gelabert  :email: <linesta@iti.es> | <luiinge@gmail.com>

[[back to top](#configurer)]

License
-----------------------------------------------------------------------------------------
MIT License

Copyright (c) 2020 Luis Iñesta Gelabert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

[[back to top](#configurer)]

References
-----------------------------------------------------------------------------------------

- [**1**] *Apache Commons Configuration* - https://commons.apache.org/proper/commons-configuration

[1]:  https://commons.apache.org/proper/commons-configuration
