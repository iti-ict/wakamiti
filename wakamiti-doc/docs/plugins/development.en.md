---
title: Plugin development
date: 2026-03-30
slug: /en/plugins/development
---

## Recommended approach

- Create and version your plugin (for example, `myplugin-wakamiti-plugin`).
- Publish the artifact to a Maven repository (local, Nexus, Artifactory, or Maven Central).
- Load it in Wakamiti as an external module.

## Plugin types

### Steps plugin (`steps`)

Use it when you want to provide a Gherkin steps catalog.

What to include:

- A class implementing `StepContributor` with methods annotated with `@Step`.
- A `ConfigContributor<YourStepContributor>` if you need default configuration or wiring.
- `.properties` files mapping `@Step` keys to Gherkin phrases.

If you define a Java module (`module-info.java`), register `provides`:

```java
provides es.iti.wakamiti.api.extensions.StepContributor with com.mycompany.MyStepContributor;
provides es.iti.wakamiti.api.extensions.ConfigContributor with com.mycompany.MyStepConfigContributor;
```

### Report plugin (`report`)

Use it when you want to generate result outputs (HTML, JSON, external integrations, etc.).

What to include:

- A class implementing `Reporter` and its `report(PlanNodeSnapshot rootNode)` method.
- A `ConfigContributor<YourReporter>` for output parameters, paths, or generation strategy.

If you define a Java module (`module-info.java`), register `provides`:

```java
provides es.iti.wakamiti.api.extensions.Reporter with com.mycompany.MyReportGenerator;
provides es.iti.wakamiti.api.extensions.ConfigContributor with com.mycompany.MyReportConfigContributor;
```

## Generate the plugin scaffold

You can use the official archetype:

```shell copy=true
mvn archetype:generate -DarchetypeGroupId=es.iti.wakamiti -DarchetypeArtifactId=wakamiti-plugin-maven-archetype -DarchetypeVersion=1.1.0
```

Important wizard properties:

- `groupId`: your organization Maven group.
- `artifactId`: plugin Maven artifact.
- `pluginId`: plugin functional prefix (lowercase, stable).
- `wakamitiApiVersion` and `wakamitiCoreVersion`: versions compatible with your runtime stack.

## Publish and consume externally

After packaging and publishing your plugin, add it as an external module.

Example in `wakamiti.yaml`:

```yaml
wakamiti:
  launcher:
    modules:
      - com.mycompany.wakamiti:myplugin-wakamiti-plugin:1.0.0
```

More details on module resolution: [`wakamiti.launcher.modules`](en/wakamiti/architecture#wakamitilaunchermodules).

## Release checklist

- Semantic versioning (`1.0.0`, `1.1.0`, ...).
- CI pipeline running tests and publishing artifacts.
- `CHANGELOG.md` and plugin configuration docs.
- Explicit compatibility matrix for supported Wakamiti versions.
