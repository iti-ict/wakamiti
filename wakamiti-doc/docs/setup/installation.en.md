---
title: Installation
date: 2022-09-20
slug: /en/setup/installation
---

This guide explains how to run Wakamiti quickly and verify that the installation works.

## Prerequisites

- [Docker](https://www.docker.com/get-started/) installed and running.
- A project folder containing your `.feature` files and `wakamiti.yaml`.
- Internet access to download the Docker image and external dependencies.

## Run Wakamiti with Docker

From your project folder, run:

Windows:
```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

Linux:
```shell copy=true
docker run --rm -v "$(pwd):/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```

## Use a specific Wakamiti version

If you need a fixed version for reproducible runs:

```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti:<version>
```

Available tags: [Wakamiti Docker Hub](https://hub.docker.com/r/wakamiti/wakamiti/tags)

## Validate the installation

After execution, confirm:

- The process ends with test results in the console.
- Output files are generated (for example, `wakamiti.json` and `wakamiti.html` if reporting is enabled).

## Next steps

- Learn the recommended configuration in [Configuration](en/setup/configuration).
- Follow the execution workflow in [Usage](en/setup/usage).
- Review full engine options in [Architecture](en/wakamiti/architecture).
