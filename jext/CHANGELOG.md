# jExt - Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Now clients can manage extensions without register them in the Java extension mechanism, implementing custom extension 
loaders (e. g. using a bean manager). Those extension classes must be annotated with ```externallyManaged = true```.  

  
## [1.0.0] 2019-04-03

- Initial release.  
