# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].


## [unreleased]

### Fixed
- Connection error at startup when no configuration is provided. [#291]


## [3.2.1] - 2024-10-07

### Fixed
- Boolean columns issues.


## [3.2.0] - 2024-09-09

### Fixed
- Row with all values set to null in xls file. [#267]
- DateTime timezone issues.

### Added
- Duration data type.
- More information is added to the error trace when compared to the closest matching record.

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`
- Moved map collector to `wakamiti-api`


## [3.1.0] - 2024-04-16

### Fixed
- Step definitions and translations.

### Added
- Procedure post-execution.

### Changed
- Upgrade dependencies: `wakamiti-plugin-starter`


## [3.0.0] - 2024-03-22

### Fixed
- Conflicts in regular step expressions.
- SQL parser issues.
- Data comparison issues.

### Added
- Aliases identify multiple connections.
- Data selection steps.
- Procedure execution steps.
- Similar row selection when not found.

### Deleted
- Removed enum `CaseSensitivity`. Entity names are now automatically obtained from the database.


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


## [2.2.2] - 2023-07-27

### Fixed
- Steps conflict between `db.assert.table.count.sql.where` and `db.assert.table.count.data`


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
- Use absolute file paths based on working dir


## [2.0.1] - 2023-06-06

### Fixed
- `extractSingleResult` is nullable and a `NullPointerException` could be thrown


## [2.0.0] - 2023-05-22

### Modified
- Renamed packages to ```es.iti.wakamiti.*```


## [1.3.0] - 2022-01-26

## Modified
- The where clause is moved from a parameter to document in the following steps:
  - `db.assert.table.exists.sql.where`
  - `db.assert.table.not.exists.sql.where`
  - `db.assert.table.count.sql.where`


## [1.2.1] - 2022-02-07

### Fixed
- Setting `database.enableCleanupUponCompletion` to `true` no longer throws an exception


## [1.2.0] - 2022-01-03

### Modified
- Version aligned with `wakamiti-core:1.2.0`

### Fixed
- When no primary key is found, an error is thrown to prevent all records being deleted.


## [1.1.0] - 2021-09-17

### Modified
- Version aligned with `wakamiti-core:1.1.0`
- Use of the `jsqlparser` library

### Fixed
- Fixed some step translations


## [1.0.0] - 2019-04-03

Initial release.  


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>