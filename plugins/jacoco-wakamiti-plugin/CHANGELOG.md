# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].


## [Unreleased]

### Added
- Support multiple JaCoCo agents through `jacoco.dump.hosts`, combining their execution data per scenario.
- Add `jacoco.report.merge` to generate aggregate `.exec`, XML, CSV and HTML reports while preserving individual
  reports.
- Allow multiple class and source directories in `jacoco.report.classes` and `jacoco.report.sources`.

### Changed
- Replace `jacoco.dump.host` and `jacoco.dump.port` with the required `jacoco.dump.hosts` list using `host:port`
  entries.
- Require configured dump and report output paths to be existing directories.


## [1.2.0] - 2026-09-10

### Added
- Generate coverage segments for executed feature lifecycle hooks.

### Changed
- Upgrade dependencies: `org.ow2.asm:asm`


## [1.1.0] - 2026-07-31

### Changed
- Require Java 17.
- Upgrade Wakamiti Engine, JaCoCo and ASM.
- Publish an automatic module name instead of an explicit JPMS descriptor.


## [1.0.0] - 2025-09-19 

Initial release.


[1]: <https://keepachangelog.com>
[2]: <https://semver.org>
