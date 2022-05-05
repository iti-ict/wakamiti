# Kukumo::REST - Changelog


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

## [Unreleased]

## Fixed
- #6 : Comparador JSON no ignora el orden

## [1.3.0] 2022-01-27

## Added
- Steps to validate responses against an schema (either JSON Schema or XML Schema)


## [1.2.0] 2021-10-21

### Modified
- Version aligned with `kukumo-core:1.2.0`
- New step to set headers in requests.
- New step to set path parameters in requests.
### Fixed
- Fixed JSON comparator when lists are present in STRICT_ANY_ORDER mode.

## [1.1.0] 2021-09-17

### Modified
- Version aligned with `kukumo-core:1.1.0`
### Fixed
- Fixed bug counting SUCCESS test cases
- Fixed some step translations

  
## [1.0.0] 2019-04-03

Initial release.  


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>