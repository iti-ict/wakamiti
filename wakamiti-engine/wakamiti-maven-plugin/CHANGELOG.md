# CHANGELOG


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

## [Unreleased]

### Added
- `StepPropertyEvaluator` allows to get the result of a step using a negative index

### Changed
- Upgrade dependencies: `es.iti.wakamiti:wakamiti-core`

### Removed
- Unused dependencies: `org.slf4j:slf4j-api`, `org.apache.maven:maven-model`, `org.apache.maven:maven-artifact`, 
`org.apache.maven:maven-builder-support`, `org.apache.maven:maven-resolver-provider`, 
`org.apache.maven:maven-repository-metadata`, `org.apache.maven:maven-model-builder`, 
`org.apache.maven.resolver:maven-resolver-api`

## [2.0.0] - 2023-05-22

### Modified
- Renamed packages to ```es.iti.wakamiti.*```

## [1.4.1] 2022-10-25

### Added
- Configuration field `testFailureIgnore`
- Tests

### Fixed
- The pipeline was broken when wakamiti tests failed

## [1.2.1] 2021-10-22

### Modified
- Version aligned with `wakamiti-core:1.2.1`

## [1.2.0] 2021-09-28

### Modified
- Version aligned with `wakamiti-core:1.2.0`

## [1.1.0] 2021-09-17

### Modified
- Version aligned with `wakamiti-core:1.1.0`

## [1.0.0] 2019-04-03

Initial release.  


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>