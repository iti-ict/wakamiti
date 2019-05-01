#jExt

jExt is a simple library that allows you to define a *plug-in* architecture detecting the
Java classes that implements a specific interface. It mostly relies on the standard 
[extension mechanism](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html) provided
by Java by means of the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
class, but it simplifies and enhances the usage.

The key concepts are:
* **Extension points** (interfaces annotated with `@ExtensionPoint`) which define the contract
* **Extensions** (classes annotated with `@Extension` and implementing the extension point class)
* **Extension managers** that are responsible of retrieve extensions for an specific extension point

What is the extra value then?

* Annotation processing of `@Extension` and `@ExtensionPoint` so the classes are 
automatically registered as services by the compiler
* Capability of using multiple class loaders at the same time
* Explicitly declaring the extension point version that implement each extension, avoiding using wrong jar versions 
* Custom control about creating new extension instances or reusing existing ones
* Fine extension filtering using white lists and/or black lists
* When extending an extension via inheritance, you may either override the parent class or use both superclass and 
subclass independently  
 

## Prerequisites
jExt requires Java 8 or newer.


## Installing
Include the following within the `<dependencies>` section of your `pom.xml` file:
```xml
<dependency>
    <groupId>iti.commons</groupId>
    <artifactId>jext</artifactId>
    <version>1.0.0</version>
</dependency>
```
## License


## Authors
* Luis Iñesta - [luiinge@gmail.com](mailto:luiinge@gmail.com)







