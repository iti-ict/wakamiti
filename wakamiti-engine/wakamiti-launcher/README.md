# Wakamiti Launcher

`wakamiti-launcher` is the standalone command-line entry point for running Wakamiti without Maven installed on the
target machine. The launcher resolves the requested modules from Maven repositories, updates the runtime classpath and
executes the resulting plan.

## Runtime requirements

- Java 11 or newer
- Access to a Maven repository

The distributed binaries do not bundle a JRE. You need a local Java installation available in `PATH`, or reachable
through your usual shell setup.

## Distribution artifacts

The module produces two distribution outputs:

- `target/staging/`: unpacked launcher distribution
- `target/wakamiti-bin-{version}.zip`: ZIP distribution for releases

Build them with:

```bash
./mvnw -pl wakamiti-engine/wakamiti-launcher -am package -DskipTests
```

## Installed layout

The ZIP contains the following layout:

```text
wakamiti
wakamiti.bat
launcher.properties
java-version-checker.jar
wakamiti-launcher.jar
lib/
```

## Installation

### Linux

1. Extract `wakamiti-bin-{version}.zip` to a target directory, for example `/opt/wakamiti`.
2. Ensure the launcher script is executable:

```bash
chmod +x /opt/wakamiti/wakamiti
```

3. Define `WAKAMITI_HOME`:

```bash
export WAKAMITI_HOME=/opt/wakamiti
```

4. Add the installation directory to `PATH`:

```bash
export PATH="$PATH:$WAKAMITI_HOME"
```

5. Optionally persist both variables in your shell profile.

### Windows

1. Extract `wakamiti-bin-{version}.zip` to a target directory, for example `C:\Tools\Wakamiti`.
2. Define `WAKAMITI_HOME`:

```powershell
setx WAKAMITI_HOME "C:\Tools\Wakamiti"
```

3. Add the installation directory to `PATH`:

```powershell
setx PATH "$($env:Path);C:\Tools\Wakamiti"
```

4. Open a new terminal.

The Windows entry point is `wakamiti.bat`.

## Configuration

The launcher reads configuration from three sources, in this order:

1. `launcher.properties` next to `wakamiti-launcher.jar`
2. project configuration file, `wakamiti.yaml` by default
3. command-line overrides

The bundled `launcher.properties` enables the default remote Maven repository:

```properties
mavenFetcher.useDefaultRemoteRepository=true
```

You can override Maven Fetcher settings there, for example custom repositories, local repository path or proxy
configuration.

The launcher caches downloaded artifacts in a platform-specific user data directory managed by `appdirs`. It no longer
uses a `.wakamiti` folder in the current working directory.

## Basic usage

Show help:

```bash
wakamiti -h
```

Run using modules passed on the command line:

```bash
wakamiti --modules es.iti.wakamiti:wakamiti-gherkin:{version},es.iti.wakamiti:rest-wakamiti-plugin:{version}
```

`--modules` expects a comma-separated list.

Run using a project configuration file:

```yaml
wakamiti:
  launcher:
    modules:
      - es.iti.wakamiti:wakamiti-gherkin:{version}
      - es.iti.wakamiti:rest-wakamiti-plugin:{version}
```

By default the launcher reads `./wakamiti.yaml`. Use `--file` to point to a different file:

```bash
wakamiti --file config/my-wakamiti.yaml
```

## Command-line options

The current CLI options are:

| Option | Description |
| --- | --- |
| `-h`, `--help` | Show help |
| `-d`, `--debug` | Enable debug logging |
| `-c`, `--clean` | Force downloaded modules to be re-fetched |
| `-f`, `--file <path>` | Use a custom configuration file |
| `-m`, `--modules <g:a:v,...>` | Comma-separated module coordinates |
| `-n`, `--dry-run` | Generate the report without executing the plan |
| `-l`, `--list` | Show available contributions after module resolution |
| `-K key=value` | Override a `wakamiti.*` property |
| `-M key=value` | Override a `mavenFetcher.*` property |
| `-a`, `--ai` | Activate feature generator mode |
| `-D`, `--apiDocs <path-or-url>` | API docs input for feature generation |
| `-t`, `--token <token>` | Token used by feature generation |
| `-p`, `--path <path>` | Output path for generated features |
| `-L`, `--language <code>` | ISO 639-1 language code for feature generation |

## Examples

List contributions exposed by the resolved modules:

```bash
wakamiti --modules es.iti.wakamiti:wakamiti-gherkin:{version} --list
```

Force a fresh resolution of modules:

```bash
wakamiti --clean --modules es.iti.wakamiti:wakamiti-gherkin:{version}
```

Run with explicit property overrides:

```bash
wakamiti \
  --modules es.iti.wakamiti:wakamiti-gherkin:{version} \
  -KoutputFilePath=build/reports/wakamiti.json \
  -MmavenFetcher.remoteRepositories=internal=https://repo.example.com/maven2/
```

## License

```text
Mozilla Public License 2.0

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.
```
