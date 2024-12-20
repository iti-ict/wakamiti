---
title: Azure Integration
date: 2023-07-04
slug: /en/plugins/azure
---


By attaching files (such as those generated by the HTML Reporter plugin) to the [Azure](https://azure.microsoft.com/) 
run, this plugin integrates the results of a Wakamiti execution into an existing Azure test plan.

---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:azure-wakamiti-plugin:2.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>azure-wakamiti-plugin</artifactId>
  <version>2.0.0</version>
</dependency>
```


## Options


###  `azure.enabled`
- Type: `boolean`
- Default `true`

Enable/disable entirely this plugin.

Example:
```yaml
azure:
  enabled: false
  
```


###  `azure.baseURL`
- Tipo: `url` *required*

The base url where the Azure server is located.

Example:
```yaml
azure:
  baseURL: https://azure.mycompany.org
```


###  `azure.auth.username`
- Type: `string` 

Username to be used with the Azure REST API, passed as an HTTP basic authentication.

Example:
```yaml
azure:
  auth:
    username: myuser
```


###  `azure.auth.password`
- Type: `string` *required*

Password to be used with the Azure REST API, passed as an HTTP basic authentication.

Example:
```yaml
azure:
  auth:
    password: xKHJFHLKJ7897
```


### `azure.auth.token`
- Type: `string`

The token to be used in the Azure REST API, passed as an HTTP basic authentication.

Example:
```yaml
azure:
  auth:
    token: s3cr3t
```


###  `azure.apiVersion`
- Type: `string` 
- Default: `6.0-preview`

The Azure REST API version to be used to send the notifications.

> Using an older version may result in certain functionality, such as creating new test cases, not working correctly.

Example:
```yaml
azure:
  apiVersion: '6.0-preview'
```


###  `azure.organization`
- Type: `string` *required*

The name of the organisation in Azure to which the test plan belongs.

Example:
```yaml
azure:
  organization: MyOrganization
```


###  `azure.project`
- Type: `string` *required*

The name of the Azure project that the test plan belongs to.

Example:
```yaml
azure:
  project: MyProject
```


### `azure.plan.name`
- Type: `string` *required*

Test plan name in Azure.

Example:
```yaml
azure:
  plan: 
    name: Wakamiti Test Plan
```


### `azure.plan.area`
- Type: `string` *required*


The area path to which the plan belongs.

Example:
```yaml
azure:
  plan: 
    area: ABC/DE
```


### `azure.plan.iteration`
- Type: `string` *required*

The iteration path of the Plan.

Example:
```yaml
azure:
  plan: 
    area: ABC/Iteration 1
```


### `azure.suiteBase`
- Type: `path`

Azure suites are set based on the path where the features are located. This property allows you to set the path from 
which the routes will be used to create the suites.

You can also manually specify the test suite [from the feature](#azuresuite).

Example:
```yaml
azure:
  suiteBase: features
```


###  `azure.attachments`
- Type: `glob[]`

Glob patterns of the reports you want to attach.

Example:
```yaml
azure:
  attachments: 
    - '**/*.html'
    - '**/wakamiti.json'
```


### `azure.testCasePerFeature`
- Type: `boolean`
- Default: `false`

Specifies whether the mapping to Azure test cases should be at feature level or scenario level.

Example:
```yaml
azure:
  testCasePerFeature: true
```


###  `azure.createItemsIfAbsent`
- Type: `boolean`
- Default: `false`

Set whether to automatically create elements (plans, suites, and test cases) that do not exist in Azure.

Example:
```yaml
azure:
  createItemsIfAbsent: true
```



## Usage

Synchronisation with Azure is performed before the tests are run. If there is a problem, the execution is stopped.
At the end of the execution, the results are synchronised with azure.


### `azureSuite`
- Type: `path`

Sets the suite in which the test will be located in azure.

Example:
```gherkin
# azureSuite: My Suite/Subsuite a
Feature: User creation tests

Scenario: Create a non-existing user
...

Scenario: Create an existing user
...
```
