# Maven Fetcher

This project intention is to offer an easy way to retrieve Maven artifacts from remote repositories outside a regular 
Maven life-cycle operation. This feature may be necessary, for example, if you are building a tool that handles plugins
that are provided as Maven artifacts, and you need to download and including them in your classpath dynamically.

There is already a library solving this situation: [Aether]. However, its API is proven to be a bit overwhelming 
for casual clients. The *Maven Fetcher* library is **wrapper** around it exposing a simpler API that should be enough for
basic usage, as well as adding the capability of configure the fetching process externally using property files.


## Prerequisites
- Java 8 or newer

## Usage

### Dependency

#### Maven
Include the following within the `<dependencies>` section of your `pom.xml` file:
```xml
<dependency>
    <groupId>iti.commons</groupId>
    <artifactId>maven-fetcher</artifactId>
    <version>1.0.0</version>
</dependency>
```


### Configuration

#### Configuration via API

The API offers a set of methods to configure options fluently:

- `logger(logger :Logger)`
    
  > Set a custom SLF4J Logger instance to log the traces  
   
- `proxyURL(url :string)`

  > Set a proxy URL, if required
     
- `proxyCredentials(username: string, password: string)`

  > Set the proxy credentials, if required
  
- `proxyExceptions(exceptions: Collection<String>)`

  > Set a list of URL exceptions for the proxy, if required  

- `localRepositoryPath(localRepositoryPath: Path)`

  > Set the path of the Maven local repository folder    

- `addRemoteRepository(repositoryId: string, repositoryURL: string)`

  > Add a Maven remote repository from where retrieve artifacts

#### Configuration via properties 

Another way to configure the fetcher is load either a `Properties` object or an external  `.properties` file:
 
- `config(properties: Properties)`

   >  Configure the fetcher using the properties from the passed object

- `config(propertiesFile: Path)`

   >  Configure the fetcher using the properties from the specified file (must be a plain `.properties` file)
   
The accepted properties are the following:

| Property             | Description                                        |
| -------------------- | -------------------------------------------------- |
| `remoteRepositories` | A list of remote repository URL separated with `;` |
| `localRepository`    | The path of the Maven local repository folder      |
| `proxy.url`          | A proxy URL, if required                           |
| `proxy.username`     | The username for proxy credentials                 |
| `proxy.password`     | The password for proxy credentials                 |
| `proxy.exceptions`   | A list of proxy exceptions separated with `;`      |



### Example

A typical use of this library would be a two-step process:
1. Configure the fetcher using one the methods discussed above
1. Invoke the method  `fetchArtifacts(artifactCoordinates: Collection<String>, scopes: Collection<String>, retrieveOptionals: boolean)`

as it is shown in the following piece of code:
```java
  new MavenFetcher()
    .localRepositoryPath("/home/linesta/.m2/repository")
    .addRemoteRepository("central", "https://repo1.maven.org/maven2/")
    .fetchArtifacts(
      Arrays.asList( // coordinates of artifacts to be retrieved
         "junit:junit:4.12",
         "org.apache.commons:commons-lang3:3.9"
      ),
      Arrays.asList( // accepted scopes for inner dependencies
        "compile",
        "provided"
      ),
      false // do not retrieve optional depedencies     
    );       
```


## License
```
    Mozilla Public License 2.0

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at https://mozilla.org/MPL/2.0/.
```


## Contributing
Currently the project is closed to external contributions but this may change in the future.

## Authors
- Luis Iñesta Gelabert  |  :email: <linesta@iti.es> | :email: <luiinge@gmail.com>


[Aether]: https://projects.eclipse.org/projects/technology.aether