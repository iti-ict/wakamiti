# CHANGELOG

Global-level project changes. To check changes specific to a component,
read the `CHANGELOG` in the corresponding folder.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

## [1.3.1] 

- Added a *Bill of Materials* project (`kukumo-bom`). All artifacts versions should
be defined here and **not** specified in the corresponding subprojects. This way, the 
dependency conflicts that might occur when assembling components should be minimized.
- Removed some satellite projects from `kukumo-tools` and use the public versions instead.
- Included utility scripts for install/update and collect plugin dependencies
- Included the [Flatten Maven plugin](https://www.mojohaus.org/flatten-maven-plugin/) in 
the build lifecycle, to solve some issues in the installation stage due to the use of
`${revision}` in child poms.


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>