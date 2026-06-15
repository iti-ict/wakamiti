![](images/logo_wakamiti_bright.svg)

---

[![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/project/overview?id=iti-ict_kukumo)
[![Sonar Tests](https://img.shields.io/sonar/tests/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?metric=tests&view=list&id=iti-ict_kukumo)
[![Sonar Coverage](https://img.shields.io/sonar/coverage/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?metric=Coverage&view=list&id=iti-ict_kukumo)
[![Sonar Technical Debt](https://img.shields.io/sonar/tech_debt/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?metric=sqale_index&view=list&id=iti-ict_kukumo)
[![Docker Image Version](https://img.shields.io/docker/v/wakamiti/wakamiti?label=docker&logo=docker)](https://hub.docker.com/r/wakamiti/wakamiti)
[![Maven Central Version](https://img.shields.io/maven-central/v/es.iti.wakamiti/wakamiti-engine?logo=circle&logoColor=red)](https://mvnrepository.com/search?q=wakamiti)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/iti-ict/wakamiti)


Wakamiti is a BDD-style black-box testing framework centered on Gherkin test plans, pluggable step providers and multiple launchers for local or build-driven execution.

The repository also contains the core engine, the Maven and JUnit launchers, the standalone launcher, the VS Code extension, the plugin archetype, example projects and the internal `jext` extension library.

The published documentation site lives at <https://iti-ict.github.io/wakamiti/>. This repository-level README focuses on what is actually in this tree today.

## Latest plugin versions

| artifact                                            | version                                                                                                                                                                                                                         |
|-----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `es.iti.wakamiti:rest-wakamiti-plugin`              | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/rest-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/rest-wakamiti-plugin/latest)                           |
| `es.iti.wakamiti:db-wakamiti-plugin`                | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/db-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/db-wakamiti-plugin/latest)                               |
| `es.iti.wakamiti:html-report-wakamiti-plugin`       | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/html-report-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/html-report-wakamiti-plugin/latest)             |
| `es.iti.wakamiti:cucumber-exporter-wakamiti-plugin` | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/cucumber-exporter-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/cucumber-exporter-wakamiti-plugin/latest) |
| `es.iti.wakamiti:allure-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/allure-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/allure-wakamiti-plugin/latest)                       |
| `es.iti.wakamiti:file-uploader-wakamiti-plugin`     | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/file-uploader-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/file-uploader-wakamiti-plugin/latest)         |
| `es.iti.wakamiti:amqp-wakamiti-plugin`              | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/amqp-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/amqp-wakamiti-plugin/latest)                           |
| `es.iti.wakamiti:groovy-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/groovy-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/groovy-wakamiti-plugin/latest)                       |
| `es.iti.wakamiti:azure-wakamiti-plugin`             | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/azure-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/azure-wakamiti-plugin/latest)                         |
| `es.iti.wakamiti:appium-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/appium-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/appium-wakamiti-plugin/latest)                       |
| `es.iti.wakamiti:email-wakamiti-plugin`             | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/email-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/email-wakamiti-plugin/latest)                         |
| `es.iti.wakamiti:io-wakamiti-plugin`                | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/io-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/io-wakamiti-plugin/latest)                               |
| `es.iti.wakamiti:jmeter-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/jmeter-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/jmeter-wakamiti-plugin/latest)                       |
| `es.iti.wakamiti:jacoco-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/jacoco-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/jacoco-wakamiti-plugin/latest)                       |

## Repository layout

- `wakamiti-api`: public API and shared configuration model.
- `wakamiti-engine`: engine modules such as `wakamiti-core`, `wakamiti-junit`, `wakamiti-maven-plugin`, `wakamiti-launcher`, `wakamiti-lsp` and `wakamiti-docker`.
- `plugins`: built-in Wakamiti plugins.
- `examples`: runnable sample projects and Docker-based tutorials.
- `wakamiti-vscode-extension`: editor integration built on the Wakamiti language server.
- `wakamiti-plugin-maven-archetype`: template used to bootstrap new plugins.
- `jext` and `jext-spring`: generic extension-loading libraries used by Wakamiti.

## Quick start

At minimum, a Wakamiti project needs:

1. one or more `.feature` files
2. a `wakamiti.yaml` configuration file
3. the modules that provide the step implementations you want to use

Example:

```yaml
wakamiti:
  resourceTypes:
    - gherkin

  launcher:
    modules:
      - es.iti.wakamiti:rest-wakamiti-plugin:{version}
      - es.iti.wakamiti:db-wakamiti-plugin:{version}
      - es.iti.wakamiti:html-report-wakamiti-plugin:{version}
      - com.h2database:h2:1.4.200

  outputFilePath: target/wakamiti/wakamiti.json
  htmlReport:
    output: target/wakamiti/wakamiti.html
```

Once configured, you can execute the plan with one of the supported launchers:

- the standalone CLI: see [wakamiti-engine/wakamiti-launcher/README.md](wakamiti-engine/wakamiti-launcher/README.md)
- the JUnit runner: see [wakamiti-engine/wakamiti-junit/README.md](wakamiti-engine/wakamiti-junit/README.md)
- the Maven plugin: see [wakamiti-engine/wakamiti-maven-plugin/README.md](wakamiti-engine/wakamiti-maven-plugin/README.md)

## Launchers

Wakamiti currently exposes three execution surfaces in this repository:

- `wakamiti-junit`: runs plans as JUnit 4 suites inside a Java test project.
- `wakamiti-maven-plugin`: integrates plan execution and report generation into the Maven lifecycle.
- `wakamiti-launcher`: standalone CLI that resolves external modules from Maven repositories at runtime.

Use the launcher that matches the way the project is already built. If the tests belong to a Java codebase, JUnit or Maven usually fit better than a separate CLI wrapper.

## Examples

The `examples/` directory reflects the current supported flows:

- `examples/junit-launcher-example`: minimal JUnit-based setup.
- `examples/spring-junit-example`: Spring Boot application tested through `wakamiti-junit`.
- `examples/spring-verify-example`: Maven `verify` / `integration-test` setup using `wakamiti-maven-plugin`.
- `examples/tutorial` and `examples/tutorial-jacoco`: Docker-based end-to-end samples.

## Editor integration

The repository includes:

- `wakamiti-engine/wakamiti-lsp`: the language server implementation.
- `wakamiti-vscode-extension`: a VS Code extension that connects either to a TCP language server or to an embedded Java process.

See [wakamiti-vscode-extension/docs/guide.md](wakamiti-vscode-extension/docs/guide.md) for installation and usage notes that match the current extension manifest.

## Building from source

Requirements:

- Java 11 or later for the main build
- Maven 3.9+ through the provided wrapper

Typical local build:

```bash
./mvnw install -DskipTests -DskipExampleTests
```

More specific setup notes are in [installation.md](installation.md).

## Contributing

Open issues and pull requests in the main repository:

- issues: <https://github.com/iti-ict/wakamiti/issues>
- source: <https://github.com/iti-ict/wakamiti>

For plugin work, start from [plugin-development-guide.md](plugin-development-guide.md).

## Acknowledgements

This software has been developed as a part of the Plan of Non-Economical Activities of
**Instituto Tecnológico de Informática (ITI)** for the year 2021, funded by
**Institut Valencià de Competitivitat Empresarial (IVACE)** and **Generalitat Valenciana**,
by means of the colaboration agreement between IVACE and ITI aimed to enhance their activity
and capabilities of developing excellence in the matter of independant R&D, spreading
the results of conducted researches, and driving knowledge transfer among companies from the
*Comunitat Valenciana*.


<img src="images/iti.png" align="right">
<img src="images/gva-ivace.png">

## License

Wakamiti is licensed under the Mozilla Public License 2.0.

Copyright © 2022 Instituto Tecnológico de Informática (ITI).

```text
Mozilla Public License 2.0

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.
```

See the [LICENSE](LICENSE.md) file for the full license text.


## Maintainers

- **María Galbis Calomarde** - mgalbis@iti.es

