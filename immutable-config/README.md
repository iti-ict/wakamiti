# Immutable Configurations


> A multi-purpose, immutable configuration interface

This library provides a simple interface in order to load and consume *configurations*,
which are mainly a set of valued properties that can be parsed to specific Java types.
The primary focus of the library is **null-safety**, **immutability**, and **fluency**.

The `Configuration` class is immutable in order to ensure the values are not modified
by any process, but it can build derived configurations. Also, when a property is not defined,
the `get` and `getList` methods return an empty `Optional` and an empty immutable `List`,
respectively, instead of `null`.

In addition, this library implements a simple approach to create *property definitions*, so that
each property in a configuration can have some meta-data regarding the actual values expected
by the consumer system.

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


Usage
-----------------------------------------------------------------------------------------

### Loading configurations

In order to obtain a configuration, simply use one of the static methods in `ConfigurationFactory`:

```java
Configuration conf = Configuration.factory().fromPath(Path.of("myConfig.yaml"));
```

Two configurations can be merged, using one of them as base:

```java
Configuration confA = Configuration.factory().fromEnvironment();
Configuration confB = Configuration.factory().fromPath(Path.of("myConfig.yaml"));
Configuration confC = confA.append(confB);
```
or, using the chainable methods:
```java
Configuration conf = Configuration.factory()
  .fromEnvironment()
  .appendFromPath(Path.of("myConfig.yaml"));
```

You can create a new configuration from Java objects:
```java
Map<String,String> map = Map.of(
    "propertyA","valueA",
    "propertyB","valueB"
);
Configuration conf = Configuration.factory().fromMap(map);
```
```java
Configuration conf = Configuration.factory().fromPairs(
    "propertyA","valueA",
    "propertyB","valueB"
);
```

In addition, you can annotate any class and use it as a configuration source:

```java
@AnnotatedConfiguration(properties={
  @Property(key="propertyA", value="valueA"),
  @Property(key="propertyB", value="valueB")
})
class MyConfigClass { }
```
```java
Configuration conf = Configuration.factory().fromAnnotation(MyConfigClass.class);
```

### Retrieving values
In order to retrieve a value, you must establish the Java type you want to get.
The supported types are:
- `String`
- `Byte`, `Short`, `Integer`, `Long`
- `Float`, `Double`
- `BigDecimal`, `BigInteger`
- `LocalTime`, `LocalDate`, `LocalDateTime` (using ISO-8601)

Notice that the `get` method returns an `Optional`, so you are forced to deal with
possible nulls. Also, you can chain mapping methods in case you want a data type
that is not automatically converted.

```java
  Configuration configuration = ... 
  Optional<String> user = configuration.get("user", String.class);
  Optional<Integer> year = configuration.get("year", Integer.class );
  Locale locale = configuration.get("language", String.class).map(Locale::new).orElse(Locale.ENGLISH);
```

#### Multi-valued properties
This library support _multi-valued properties_, that is, properties that have a list
of values instead of a single one. For that, the method `getList` works similarly to
`get` but returns a `List` (potentially empty) instead of an `Optional`.

```java
  Configuration configuration = ... 
  List<String> servers = configuration.getList("servers", String.class);
```

### Property definitions

You can also create _definitions_ to express what properties your application expects, including
some basic validations and default values. Each expected property is defined by its key, data type,
default value, and additional constraints regarding the data type.
Supported types are:

| type      | description           | additional constraints |
| --------- | --------------------- | ---------------------- |
| `text`    | plain text            | regular expression     |
| `enum`    | strict list of values |                        |ls
| `boolean` | `true` or `false`     |                        |
| `integer` | integer number        | min and/or max bounds  |
| `decimal` | decimal number        | min and/or max bounds  |

Property definitions can be either read from YAML files (as a kind of _meta-configuration_), or
created programmatically, using any of the existing methods starting with `according...` . Notice that
the definition is always applied to a `Configuration` object.

Once a configuration has a definition applied, its existing properties must abide by the definition.
Trying to retrieve a property with an illegal value, or a required property without value, would result
in an exception.

#### Loading property definitions from YAML file

Property definitions can be easily readed from YAML files using the following method:

```java
    Configuration definition = Configuration.factory().accordingDefinitionsFromPath(Path.of("my-definition.yaml"));
```

The property definition YAML file uses the following structure:
```
<property-key>:
    type: <text|enum|boolean|integer|decimal>
    [description: <optional description>]
    [requires: <true|false> (false if ommitted)]
    [defaultValue: <optional default value>]
    [constrains: (regarding the property type)
       <min | max | pattern | values>: <constraint-value> 
       ...
    ]
```

as shown is this example:

```yaml
my-properties.property-required:
   description: This is a test property that is required
   required: true
   type: text

my-properties.property-with-default-value:
   description: This is a property with a default value
   type: integer
   defaultValue: 5

my-properties.property-regex-text:
   type: text
   constraints:
      pattern: A\d\dB

my-properties.property-min-max-number:
   type: integer
   constraints:
      min: 2
      max: 3

my-properties.property-enumeration:
   type: enum
   constraints:
      values:
         - red
         - yellow
         - orange

my-properties.property-boolean:
   type: boolean
```

#### Create property definitions programmatically

If you prefer to encapsulate the properties definition within your code, it is possible to
create the definitions programatically and then apply them to a configuration.

```java
var definitions = List.of(
    PropertyDefinition.builder("my-properties.property-required")
        .description("This is a test property that is required")
        .required()
        .textType()
        .build(),
    PropertyDefinition.builder("my-properties.property-with-default-value")
        .description("my-properties.property-with-default-value")
        .integerType()
        .defaultValue(5)
        .build(),
    PropertyDefinition.builder("my-properties.property-min-max-number")
        .integerType(2,3)
        .build()
);
var configuration = Configuration.factory().accordingDefinitions(definitions);
```


### Maven dependency
```xml
<dependency>
    <groupId>immutable-config</groupId>
    <artifactId>immutable-config</artifactId>
    <version>1.1.0</version>
</dependency>
```


Requirements
-----------------------------------------------------------------------------------------
- Java 11 or newer.


## Contributing

## Authors

- Luis Iñesta Gelabert - luiinge@gmail.com


License
-----------------------------------------------------------------------------------------
MIT License

Copyright (c) 2020 Luis Iñesta Gelabert - luiinge@gmail.com

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



References
-----------------------------------------------------------------------------------------

- [**1**] *Apache Commons Configuration* - https://commons.apache.org/proper/commons-configuration

[1]:  https://commons.apache.org/proper/commons-configuration