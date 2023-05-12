# CHANGELOG

Global-level project changes. To check changes specific to a component,
read the `CHANGELOG` in the corresponding folder.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

[1.7.0] -- 2023-05-03
---------------------------------------------------------------------------------

### Wakamiti Core [1.7.0] 
#### Added
- Capability to use the execution ID as placeholder in the output file name
- Categorize test case errors according the step contributor
- New configuration property to include the filtered test cases in the plan result, as SKIPPED
#### Fixed
- StartInstant in NOT_IMPLEMENTED results
- Add the `keyword` and `description` in the Scenario Outlines nodes to prevent errors in the html report
- Fill execution ID when using `WakamitiJUnitRunner`

### HTML Report [1.5.1] 
#### Added
- Result for each feature and scenario
- Categorize test case errors according the step contributor
#### Fixed
- Step counters
- Style issues (#92)




[1.5.1] -- 2023-05-03
---------------------------------------------------------------------------------
### Added

- Result for each feature and scenario
- Categorize test case errors according the step contributor

### Fixed

- Step counters
- Style issues (#92)



## [1.3.1] 

- Added a *Bill of Materials* project (`wakamiti-bom`). All artifacts versions should
be defined here and **not** specified in the corresponding subprojects. This way, the 
dependency conflicts that might occur when assembling components should be minimized.
- Removed some satellite projects from `wakamiti-tools` and use the public versions instead.
- Included utility scripts for install/update and collect plugin dependencies
- Included the [Flatten Maven plugin](https://www.mojohaus.org/flatten-maven-plugin/) in 
the build lifecycle, to solve some issues in the installation stage due to the use of
`${revision}` in child poms.


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>