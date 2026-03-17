---
title: Usage
date: 2022-09-20
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
