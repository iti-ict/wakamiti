# Wakamiti::REST - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

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
