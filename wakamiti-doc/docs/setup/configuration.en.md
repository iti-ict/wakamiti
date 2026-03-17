---
title: Configuration
date: 2022-09-20
slug: /en/setup/configuration
---

Wakamiti is configured through a `wakamiti.yaml` file placed in your test project directory.

## File location

- Recommended location: root folder of your test project.
- Default filename: `wakamiti.yaml`.
- Test resources are read from the current path unless `wakamiti.resourcePath` is configured.

## Minimal configuration example

```yaml copy=true
wakamiti:
  resourceTypes:
    - gherkin
  launcher:
    modules:
      - es.iti.wakamiti:rest-wakamiti-plugin
      - es.iti.wakamiti:html-report-wakamiti-plugin
  htmlReport:
    title: Test execution
  rest:
    baseURL: http://localhost:8080/api
```

## Configuration structure

- `wakamiti`: global engine options (resource types, launch behavior, logs, output, tags, etc.).
- `wakamiti.launcher.modules`: external modules (plugins, JDBC drivers, custom extensions).
- Plugin blocks (`rest`, `database`, `amqp`, `htmlReport`, etc.): plugin-specific options.

## Recommended practices

- Keep `launcher.modules` explicit to avoid hidden dependencies.
- Store environment-specific values outside the repository when possible.
- Start from a minimal configuration and add options only when needed.
- Use one config file per environment if your test data and endpoints vary.

## Related references

- Full option catalog: [Architecture](en/wakamiti/architecture)
- Plugin options: [Plugins](en/plugins)
