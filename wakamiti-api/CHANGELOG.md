# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].


## [2.3.3] - 2023-09-14

### Fixed
- Add dependency `junit:juni:4.13.2` to `dependencyManagement`


## [2.3.2] - 2023-09-11

### Changed
- Pom flatten mode


## [2.3.1] - 2023-09-08

### Changed
- Moved Bill of materials dependencies


## [2.3.0] - 2023-09-08

### Changed
- Upgrade dependencies: `es.iti.commons:jext`, `org.apache.groovy`

### Removed
- Unused dependencies: `xerces:xercesImpl`, `org.apache.poi:poi-ooxml`, `org.xmlunit:xmlunit-core`

### Fixed
- JsonUtils and XmlUtils issues when return null values


## [2.1.1] - 2023-07-07

### Fixed
- Property `workingDir`


## [2.1.0] - 2023-07-05

### Added
- Extra properties in Backend context
- Delete WakamitiVersion class
- Add WakamitiLogger forName method
- New property `workingDir`


## [2.0.1] - 2023-06-22

### Fixed
- Output field `errorClassifier` was not included when `outputFilePerTestCase = true`


## [2.0.0] - 2023-05-22

### Added
- Gpath expressions in json and xml evaluators

### Modified
- Renamed packages to ```es.iti.wakamiti.*```
- Default configuration file is now ```wakamiti.yaml```


## [1.5.1] - 2023-04-20

### Added
- Include error classifiers in result


## [1.5.0] - 2023-01-17

### Added
- Property evaluators to read global properties and the results of previous steps [issue: #66]


## [1.4.0] - 2022-05-09

Initial release.  


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>