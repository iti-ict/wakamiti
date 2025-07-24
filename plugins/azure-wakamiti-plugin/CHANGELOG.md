# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].


## [unreleased]

### Changed
- The identifier is now incorporated into the name of the test case, in the 
  format `[ID-XX] Scenario Name`.


## [2.1.2] - 2025-02-26

### Fixed
- The endpoint `/{organization}/{project}/_apis/test/Runs/{runId}/results` 
  only returns a maximum of 1000 records.


## [2.1.1] - 2025-02-24

### Fixed
- Concurrency limit of 100 every 10,000 milliseconds.


## [2.1.0] - 2024-12-11

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


## [2.0.4] - 2024-11-27

### Removed
- Azure tag functionality.

### Added
- Glob pattern setting functionality for attachments.


## [2.0.3] - 2024-11-26

### Fixed
- All tests run regardless of the azure tag.


## [2.0.2] - 2024-11-25

### Fixed
- The RunTest tag could not be saved.


## [2.0.1] - 2024-11-25

### Fixed
- `WorkItemUnauthorizedSuppressNotificationsException` when creating tests.
- Test suite path escape.


## [2.0.0] - 2024-11-25

### Fixed
- Escaping double quotes in Azure names. (#218)
- Error messages without error detail. (#233)

### Changed
- Plugin refactor
- Upgrade dependencies: `wakamiti-plugin-starter`


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

### Changed
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

### Changed
- Change plugin nomenclature




[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>