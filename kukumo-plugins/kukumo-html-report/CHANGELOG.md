# Kukumo::HTML Report - Changelog


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1],
and this project adheres to [Semantic Versioning][2].

## [1.3.3] 20220506
#### Fixed
- Fix #26 : HTML escaping in descriptions and error messages
- Fix : Report title was not properly passed to the template engine

## [1.2.1] 2021-10-22
#### Fixed
- The base packaging path of `TemplateClassLoader` now is `/`

## [1.2.0] 2021-09-29
### Modified
- Rework of internal implementation. Switched from `j2html` to
`freemarker`. 
- Now requires version `kukumo-core:1.2.0` or newer


## [1.1.0] 2021-09-17

### Modified
- Version aligned with `kukumo-core:1.1.0`

  
## [1.0.0] 2019-04-03

Initial release.  


[1]: <https://keepachangelog.com/en/1.0.0/>
[2]: <https://semver.org>