# Plugin Development Guide

This repository ships a Maven archetype to bootstrap new Wakamiti plugins. The flow below matches the modules and versions currently present in this tree.

## 1. Build the archetype locally

The archetype is not consumed from `plugins/`; it is a dedicated module:

```bash
./mvnw -pl wakamiti-plugin-maven-archetype install -DskipTests
```

That installs `es.iti.wakamiti:wakamiti-plugin-maven-archetype:1.1.0` in your local Maven repository.

## 2. Generate a new plugin module

From the repository root:

```bash
cd plugins
../mvnw archetype:generate \
  -DarchetypeGroupId=es.iti.wakamiti \
  -DarchetypeArtifactId=wakamiti-plugin-maven-archetype \
  -DarchetypeVersion=1.1.0
```

The only required input is `pluginId`. The archetype derives the usual defaults from it:

- `artifactId`: `${pluginId}-wakamiti-plugin`
- `package`: `es.iti.wakamiti.plugins.${pluginId}`
- `pluginName` and `pluginDescription`

The archetype also asks for API/Core versions. Treat those defaults as a starting point and align them with the Wakamiti release you actually want to target before publishing the plugin.

## 3. Register the module in the plugin aggregator

Add the generated module to `plugins/pom.xml`:

```xml
<modules>
    ...
    <module>myplugin-wakamiti-plugin</module>
</modules>
```

Without this step the new plugin will not be part of the aggregated plugin build.

## 4. Implement the contributor

The generated project already contains the standard scaffold:

- Maven coordinates and starter parent
- base Java sources
- test sources
- `LICENSE.md` and `CHANGELOG.md`

From there, implement the step contributor and any auxiliary services required by the plugin. Reuse the patterns from existing modules under `plugins/` rather than inventing a new registration style.

## 5. Verify the module in isolation

Run its tests directly:

```bash
./mvnw -pl plugins/myplugin-wakamiti-plugin test
```

If the plugin depends on additional project classes during execution, wire them explicitly in the plugin module or in the consumer configuration. Do not assume they will appear automatically at runtime.
