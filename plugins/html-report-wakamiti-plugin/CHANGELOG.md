# Wakamiti::HTML Report - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

## [2.5.0] - 2024-04-16

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


## [2.4.0] - 2024-03-22

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


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
- Upgrade dependencies: `es.iti.wakamiti:wakamiti-api`, `es.iti.wakamiti:wakamiti-core`


## [2.2.1] - 2023-07-27

### Fixed
- Api Groovy dependencies compatibility


## [2.2.0] - 2023-07-18

### Modified
- Change plugin nomenclature


## [2.1.1] - 2023-07-07

## Fixed
- Use absolute file paths based on working dir


## [2.1.0] - 2023-07-05

## Fixed
- Do not display error classifiers when there are no errors

## Modified
- Use absolute file paths based on working dir


## [2.0.0] - 2023-05-22

### Added
- Publish event when HTML file is generated
- Output HTML file path now accept placeholders

### Modified
- Renamed packages to ```es.iti.wakamiti.*```


## [1.5.1] -- 2023-05-03

### Added
- Result for each feature and scenario
- Categorize test case errors according the step contributor

### Fixed
- Step counters
- Style issues (#92)


## [1.5.0] - 2023-02-23

### Modified
- New template (#60, #76)


## [1.4.0] - 2022-05-09

### Modified
- API separation


## [1.3.3] - 2022-05-06

### Fixed
- HTML escaping in descriptions and error messages (#26)
- Report title was not properly passed to the template engine


## [1.2.1] - 2021-10-22

### Fixed
- The base packaging path of `TemplateClassLoader` now is `/`


## [1.2.0] - 2021-09-29

### Modified
- Rework of internal implementation. Switched from `j2html` to `freemarker`. 
- Now requires version `wakamiti-core:1.2.0` or newer


## [1.1.0] 2021-09-17

### Modified
- Version aligned with `wakamiti-core:1.1.0`


## [1.0.0] - 2019-04-03

Initial release.  


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>