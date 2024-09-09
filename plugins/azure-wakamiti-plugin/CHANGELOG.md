# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].


## [1.9.0] - 2024-09-09

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


## [1.8.0] - 2024-04-16

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


## [1.7.0] - 2024-03-22

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


## [1.5.0] - 2023-10-26

### Added
- Add configuration `azure.disabled` in order to disabled entirely the plugin
- Add property `azureTestId` to bound a feature/scenario to an existing test case in Azure

### Modified
- Test case names in Azure are updated if modified in Wakamamiti


## [1.4.2] - 2023-09-20

### Fixed
- Fix issue regarding the escape of error messages (#144)

### Changed
- All test suites are notified as a single run instead of multiples




## [1.4.1] - 2023-09-14

### Fixed
- Add dependency `junit:juni:4.13.2` to `dependencyManagement`
- Use default last `es.iti.wakamiti:wakamiti-api:2.3.3` dependency as `provided`


## [1.4.0] - 2023-09-13

### Added
- Creation of test plans, suites, and test cases in the Azure instance when absent
- Fill run data regarding start, finish, and execution time.



## [1.3.2] - 2023-09-11

### Changed
- Pom flatten mode


## [1.3.1] - 2023-09-08

### Changed
- Moved Bill of materials dependencies


## [1.3.0] - 2023-09-08

### Changed
- Upgrade dependencies: `es.iti.wakamiti:wakamiti-api`, `es.iti.wakamiti:wakamiti-core`

## [1.2.1] - 2023-07-27

### Fixed

- Api Groovy dependencies compatibility

## [1.2.0] - 2023-07-18

### Modified

- Change plugin nomenclature




[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>