# Wakamiti::REST - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].


## [2.9.0] - 2025-11-10

### Added
- New steps have been introduced to specify the MimeType of an attachment.


## [2.8.3] - 2025-04-03

### Fixed
- Prevent duplicated headers.


## [2.8.2] - 2024-12-13

### Fixed
- Comparing different scaled numerical json elements.


## [2.8.1] - 2024-12-12

### Fixed
- Missing `com.google.guava:failureaccess` runtime dependency in the maven plugin.


## [2.8.0] - 2024-12-11

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


## [2.7.0] - 2024-11-25

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`
- Change dependency `plexus-utils` by `commons-io`


## [2.6.0] - 2024-09-09

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`

### Fixed
- Http code assertion step definition


## [2.5.0] - 2024-04-16

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`

### Fixed
- Step definitions and translations


## [2.4.0] - 2024-03-22

### Added
- Check that base URL does not contain query parameters.

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

### Added
- Steps to submit forms (issue #152)
- Steps to object/list fragment comparison (issue #149)

### Fixed
- Attached files were not sent correctly

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

### Modified
- Use `ResourceLoader` from execution context


## [2.0.0] - 2023-05-22

### Modified
- Renamed packages to ```es.iti.wakamiti.*```


## [1.6.2] - 2023-05-03

### Fixed
- The response body was always parsed as string

### Added
- New Delete Steps with body


## [1.6.0] - 2023-01-19

### Added
- Oauth2 authentication steps with extra parameters [issue: #77]

### Fixed
- Text comparator


## [1.5.1] - 2023-01-16

### Fixed
- Step to set the contentType


## [1.5.0] - 2022-11-11

### Added
- Authentication token cache
- Oauth2 authentication steps
- Single header and parameter steps
- Specific control name in attached file step

### Fixed
- Helpers compare in any order, including lists [issue: #6]
- Timeout interrupts http call when time exceeded


## [1.4.1] - 2022-09-21

### Fixed
- Bump version of `restassured` to avoid module incompatibility with `groovy-xml`


## [1.3.3] - 2022-05-05

### Fixed
- Helpers compare in any order [issue: #6]
- Parameterized service path [issue: #20]


## [1.3.0] - 2022-01-27

### Added
- Steps to validate responses against an schema (either JSON Schema or XML Schema)


## [1.2.0] - 2021-10-21

### Modified
- Version aligned with `wakamiti-core:1.2.0`
- New step to set headers in requests.
- New step to set path parameters in requests.

### Fixed
- Fixed JSON comparator when lists are present in STRICT_ANY_ORDER mode.


## [1.1.0] - 2021-09-17

### Modified
- Version aligned with `wakamiti-core:1.1.0`

### Fixed
- Fixed bug counting SUCCESS test cases
- Fixed some step translations


## [1.0.0] - 2019-04-03

Initial release.  


[1]: <https://keepachangelog.com>
[2]: <https://semver.org>
