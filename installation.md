# Installation

The preferred installation path is to download the standalone launcher ZIP published with each core release. Building from source is only needed for local development or when you want to produce the distribution yourself.

Supported paths:

- install the release ZIP
- build from source and package the launcher yourself
- use the Docker image when a containerized run is enough

## Prerequisites

- Java 11 or later
- Docker only if you plan to use the container image

Maven is only required for the source-build path.

## Install from release ZIP

Each core release publishes a ZIP with the standalone launcher binaries already prepared for installation.

1. Download `wakamiti-bin-{version}.zip` from the release assets.
2. Extract it to the target directory.
3. Configure `WAKAMITI_HOME` and add that directory to `PATH`.

The installed layout and platform-specific steps are documented in [wakamiti-engine/wakamiti-launcher/README.md](wakamiti-engine/wakamiti-launcher/README.md).

## Build from source

From the repository root:

```bash
./mvnw install -DskipTests -DskipExampleTests
```

That installs the Maven artifacts in your local repository and makes them available to:

- example projects in `examples/`
- locally generated plugins
- launcher runs that resolve modules from `~/.m2/repository`

## Package the launcher from source

If you need to generate the distributable ZIP yourself:

```bash
./mvnw -pl wakamiti-engine/wakamiti-launcher -am package -DskipTests
```

The generated ZIP is written under:

```text
wakamiti-engine/wakamiti-launcher/target/wakamiti-bin-{version}.zip
```

That artifact is the same installation format expected for release delivery.

## Run with Docker

For Docker-based tutorials and isolated executions, the examples in this repository use the `wakamiti/wakamiti` image. A typical run mounts a local test project at `/wakamiti`:

```bash
docker run --rm -v "$(pwd):/wakamiti" wakamiti/wakamiti
```

On Windows `cmd`:

```bat
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

See the example-specific READMEs in `examples/tutorial` and `examples/tutorial-jacoco` for working Docker compositions.

## Refresh resolved modules

The standalone launcher caches downloaded artifacts in an OS-specific user data directory managed by `appdirs`. To force a fresh resolution, prefer the built-in flag:

```bash
wakamiti --clean
```

That is safer than relying on undocumented cache paths.
