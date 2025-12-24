# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [2.8.0] - 2025-12-24

### Changed
- Docker base image.


## [2.7.2] - 2024-12-18

### Fixed
- File parsing error localization. (#332)
- The table of examples in the outline scenarios is mandatory. (#338)


## [2.7.1] - 2024-12-11

### Fixed
- ChatGPT-based feature generator issues.


## [2.7.0] - 2024-11-25

### Added
- New command to export reports without running the tests. (#243)
- X11 server mode. (#298)
- ChatGPT-based Feature generator.

### Fixed
- The log file was not being generated.


## [2.6.3] - 2024-09-16

### Fixed
- Inter-module conflict due to a bug in the `maven-fetcher` library.

### Changed
- The functionality of the `maven-fetcher` library is included in the core.


## [2.6.2] - 2024-09-12

### Fixed
- Missing dependencies with `runtime` scope in `maven-fetcher`.


## [2.6.1] - 2024-09-12

### Fixed
- Module `duration-types` is included in the default modules when there are restricted modules.
- Force the Gherkin parser to throw an error when a feature or scenario is untitled.


## [2.6.0] - 2024-09-09

### Added
- Duration data type and Duration assertion.
- DateTime assertion.
- DateTime `yyyy-MM-dd[ hh:mm:ss[.SSS][Z]]` to ISO 8601 DateTime formats

### Changed
- Datatype AbstractProvider and ExpressionMatcher are moved to the api library.
- The StepPropertyEvaluator accepts both step index and the step `id`.
- Step nodes have ids.
- The features can have a `tagId` and will be checked for uniqueness, as will test cases if the `strictTestCaseID` 
  option is enabled.

### Fixed
- The junit wakamiti configuration is modified to set the default output path of the current project.
- Reset classpath on every run of `wakamiti-maven-plugin` verify mojo.


## [2.5.0] - 2024-04-16

### Fixed
- Matcher translations.
- Reconfigure property resolvers when starting junit tests execution.


## [2.4.0] - 2024-03-22

### Added
- Version compatibility check when plugins are loaded.

### Fixed
- Configure the step contributor with specific scenario configuration.
- Errors in `setUp` and `tearDown` operations will cause the scenario to fail.
- Problems with lazy streams during execution.
- The WakamitiJUnitRunner was not picking up the results and could not notify them.

### Changed
- Upgrade dependencies: `wakamiti-starter`
- The `WakamitiAssertTypes` have a specific regex instead of `.*`.
- The JUnit functionality is detached to new module `wakamiti-junit`.


## [2.3.3] - 2023-09-14

### Fixed
- Add dependency `junit:juni:4.13.2` to `dependencyManagement`
- Suppress Log4j Error in wakamiti-maven-plugin log


## [2.3.2] - 2023-09-11

### Changed
- Pom flatten mode


## [2.3.1] - 2023-09-08

### Changed
- Moved Bill of materials dependencies


## [2.3.0] - 2023-09-08

### Added
- A dynamic property is allowed to obtain the result of a step relatively, indicating a negative number.

### Fixed
- The filtered scenarios results appear as `SKIPPED`
- Tag filter is now case-insensitive and tag expressions can contain `@` at the beginning of tag

### Changed
- Upgrade dependencies: `es.iti.wakamiti:wakamiti-api`

### Removed
- Unused dependencies: `org.slf4j:slf4j-api`, `org.apache.maven:maven-model`, `org.apache.maven:maven-artifact`,
  `org.apache.maven:maven-builder-support`, `org.apache.maven:maven-resolver-provider`,
  `org.apache.maven:maven-repository-metadata`, `org.apache.maven:maven-model-builder`,
  `org.apache.maven.resolver:maven-resolver-api`


## [2.2.1] - 2023-07-27

### Fixed
- Api Groovy dependencies compatibility


## [2.2.0] - 2023-07-18

## Modified
- Group all core components under `wakamiti-engine`


## [2.1.2] - 2023-07-07

## Fixed
- Property `workingDir`


## [2.1.1] - 2023-07-05

### Fixed
- Add compile scope to jext dependency


## [2.1.0] - 2023-07-05

### Added
- Extra properties in Backend context
- New property `workingDir`


## [2.0.0] - 2023-05-22

### Modified
- Renamed packages to ```es.iti.wakamiti.*```


## [1.7.0] - 2023-05-03

### Added
- Capability to use the execution ID as placeholder in the output file name
- Categorize test case errors according to the step contributor
- New configuration property to include the filtered test cases in the plan result, as SKIPPED

### Fixed
- StartInstant in NOT_IMPLEMENTED results
- Add the `keyword` and `description` in the Scenario Outlines nodes to prevent errors in the html report
- Fill execution ID when using `WakamitiJUnitRunner`


## [1.6.0] - 2023-02-23

### Added
- New result status NOT_IMPLEMENTED


## [1.5.0] - 2023-01-16

### Modified
- Externalize groovy classloader to a new plugin

### Added
- Property evaluators to read global properties and the results of previous steps. (#66)


## [1.4.3] - 2022-11-10

### Fixed
- Text assertion


## [1.4.2] - 2022-09-21

### Fixed
- Configure logger for Maven Fetcher after setting the configuration to avoid warning messages


## [1.4.1] - 2022-09-12

### Fixed
- Update dependency `io.github.luiinge:maven-fetcher` version `1.5.0` to `1.5.1`. (#48)


## [1.3.2] - 2022-05-03

### Modified
- Bumped vulnerable dependency `commons-io:commons-io` version `2.6` to `2.11.0`
- Bumped vulnerable dependency `junit:junit` version `4.13-rc-2` to `4.13.2`
- Bumped vulnerable dependency `com.fasterxml.jackson.core:jackson-databind` version `2.10.1` to `2.13.2.2`
- Bumped vulnerable dependency `org.apache.logging.log4j:log4j-core` version `2.13.0` to `2.17.2`


## [1.3.0] - 2022-01-26

### Added
- Steps can use property substitution using the syntax `${property.name}`
- Loading extra steps as `NON_REGISTERED_STEP_PROVIDERS` at runtime in groovy language


## [1.2.1] - 2021-10-22

### Fixed
- The `ClassLoader` now loads `StepsContributor` from maven artifacts.
- Now `RunnableBackend.runStep` catch `Throwable` errors instead of `Exception`.
- Now cli command configuration overwrite file configuration.


## [1.2.0] - 2021-09-28

### Added
- New property `childrenResults` in `PlanNodeSnapshot` that collects the result count of direct children.


## [1.1.0] - 2021-09-17

### Added
- New field to `PlanNode` called `executionID`, that is set when the test plan is 
executed. The execution id can be either set via configuration (new property `executionId`) or 
autogenerated otherwise. The same id is shared along every node in the plan, and it should be 
unique among different executions. The aim of this new field is to provide a way to distinguish
executions when they are run in parallel in a server.
- New method `WakamitiContributors#allContributors()` that returns every contributor

### Modified
- Assertion-like data types now implement their own interface `Assertion` rather than 
using `Matcher` from the *Hamcrest* library. The `MatcherAssertion` class allows mapping to
`Matcher` for convenience.
- Assertion literals `matcher.string.null` and `matcher.number.null` merged into 
`matcher.generic.null` since the translated literals are likely to be the same

### Fixed
- The contributor `ConfigContributor` now extends from `Contributor` (as it should do originally)


## [1.0.0] - 2019-04-03

Initial release.  
