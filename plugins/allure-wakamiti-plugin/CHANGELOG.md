# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].


## [1.2.0] - 2026-09-09

### Added
- Include executed lifecycle hooks in Allure results.

### Changed
- Add dependencies: `io.qameta.allure:allure-java-commons`

### Fixed
- Accept UTC ISO-8601 report timestamps when converting Wakamiti snapshot instants to Allure epoch-millis fields.


## [1.1.0] - 2026-07-31

### Changed
- Require Java 17.
- Upgrade Wakamiti Engine and Testcontainers.
- Publish an automatic module name instead of an explicit JPMS descriptor.


## [1.0.0] - 2026-06-04

Initial release.


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>
