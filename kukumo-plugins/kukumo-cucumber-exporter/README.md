Wakamiti :: Cucumber Exporter
====================================================================================================
A simple report generator that emits a JSON file with the format used by 
Cucumber. This is a convenient way of reusing some pieces of software designed to work 
with Cucumber, for example the 
[Jenkins Cucumber report plugin](https://github.com/jenkinsci/cucumber-reports-plugin).

Notice that, although Cucumber and Wakamiti share structural similarities in their 
result format, there are specific pieces of information that are not interchangeable. 
For example, Wakamiti works with test plans of unrestricted depth, whereas Cucumber expects 
a fixed three-level structure. Thus, the exported Cucumber JSON file might not be a 
faithful representation of the executed test plan.


Configuration
----------------------------------------------------------------------------------------------------

###  `cucumberExporter.outputFile`
The (relative) path and name of the generated file. Default value is `cucumber-report.json`.

Example:

```yaml
cucumberExporter:
  outputFile: my-cucumber-report.json
```

### `cucumberExporter.multiLevelStrategy`
The mapping strategy used when the Wakamiti plan has more levels than the 
expected by Cucumber. Accepted values are:
- `innerSteps`: Only the most inner steps would be included, discarding step aggregation 
levels
- `outerSteps` : Inner steps for any step aggregation would be discarded. The reported step
would be the step aggregation itself, although the potential error messages of the inner 
steps would be reported.

Default value is `innerSteps` .

Example:

```yaml
cucumberExporter:
  multiLevelStrategy: outerSteps
```