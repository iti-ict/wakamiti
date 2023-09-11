# jExt - Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.1.1] - 2023-09-08

### Changed
- Moved Bill of materials dependencies


## [1.1.0] - 2023-09-08

### Added
- Now clients can manage extensions without register them in the Java extension mechanism, implementing custom extension 
loaders (e.g. using a bean manager). Those extension classes must be annotated with `externallyManaged = true`.  

### Changed
- Upgrade dependencies: `org.slf4j:slf4j-api`

### Removed 
- Unused dependencies: `javax.annotation:javax.annotation-api`

  
## [1.0.0] 2019-04-03

- Initial release.  
