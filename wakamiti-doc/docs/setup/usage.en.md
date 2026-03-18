---
title: Usage
date: 2026-03-18
slug: /en/setup/usage
---

This page describes a practical workflow to execute and maintain Wakamiti tests.

## Typical workflow

1. Define `wakamiti.yaml` with global options and required plugins.
2. Write `.feature` files with your business scenarios.
3. Run Wakamiti from the project directory.
4. Analyze generated reports and adjust scenarios or configuration.

## Recommended project layout

```text
my-tests/
├── wakamiti.yaml
├── features/
│   ├── api/
│   │   └── users.feature
│   └── integration/
│       └── orders.feature
└── data/
    ├── request/
    └── expected/
```

## Execute from terminal

Windows:
```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

Linux:
```shell copy=true
docker run --rm -v "$(pwd):/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```

## `wakamiti-launcher` CLI usage

If you have the `wakamiti` command installed, you can run it directly with CLI options:

```shell copy=true
wakamiti [options]
```

Common examples:

```shell copy=true
# Run with a specific configuration file
wakamiti -f wakamiti.ci.yaml

# Add modules from CLI (comma-separated)
wakamiti -m es.iti.wakamiti:rest-wakamiti-plugin:3.0.0,es.iti.wakamiti:html-report-wakamiti-plugin:3.0.0

# Override configuration properties without editing yaml
wakamiti -K tagFilter="@smoke and not @ignore" -K outputFilePath=results/wakamiti.json

# Configure Maven repositories inline
wakamiti -M remoteRepositories="https://repo.maven.apache.org/maven2;file:///C:/Users/user/.m2/repository"

For the full CLI option catalog and configuration mapping, see:
[Architecture](en/wakamiti/architecture).

## Understand the result files

- `wakamiti.json`: machine-readable execution result, useful for automation.
- `wakamiti.html`: human-readable report for analysis and sharing.

## Usage tips for teams

- Keep scenarios focused on behavior, not implementation details.
- Reuse common test data and shared step patterns.
- Version-control your `.feature` files and configuration together.
- Treat failing scenarios as a quality signal and review them quickly.

## Related guides

- Quick tutorial: [Getting started](en/introduction/getting-started)
- Engine options: [Architecture](en/wakamiti/architecture)
- Available plugins: [Plugins](en/plugins)
