# Wakamiti VS Code Extension

This module contains the VS Code extension for Wakamiti projects.

## What it does

- syntax support for `*.feature`
- connection to the Wakamiti language server
- test plan overview and execution views
- execution actions from the editor and the side panel

According to the current `package.json`, the extension can connect to the language server in two modes:

- `TCP Connection`
- `Java Process`

## Development

Install dependencies:

```bash
npm install
```

Compile:

```bash
npm run compile
```

Package a VSIX:

```bash
npm run package
```

The packaging script wraps `vsce package`, so `vsce` must be available in the environment.

## Usage

See [docs/guide.md](docs/guide.md) for installation and runtime configuration.
