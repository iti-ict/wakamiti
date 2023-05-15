# Wakamiti::HTML Report - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

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

- Rework of internal implementation. Switched from `j2html` to
`freemarker`. 
- Now requires version `wakamiti-core:1.2.0` or newer

## [1.1.0] 2021-09-17

### Modified

- Version aligned with `wakamiti-core:1.1.0`

## [1.0.0] - 2019-04-03

Initial release.  


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>