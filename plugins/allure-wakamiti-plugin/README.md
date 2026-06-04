Wakamiti :: Allure Report
====================================================================================================
An Allure exporter that transforms Wakamiti execution results into `allure-results` files.

The generated directory can be consumed by Allure tools such as:

```bash
allure serve allure-results
```


Configuration
----------------------------------------------------------------------------------------------------

### `allureReport.output`
The output directory where the `*-result.json` files will be generated.
Default value is `allure-results`.

Example:

```yaml
allureReport:
  output: target/allure-results
```
