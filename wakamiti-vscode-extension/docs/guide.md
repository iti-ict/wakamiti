Instalación y uso
---------------------------------------------------------------------------------------------------

Esta guía describe el estado actual de la extensión según el `package.json` del módulo.

## Instalar la extensión

La forma soportada en este repositorio es empaquetar una VSIX e instalarla manualmente en VS Code.

1. Desde `wakamiti-vscode-extension/`, compilar y empaquetar:

   ```shell
   npm install
   npm run package
   ```

2. En VS Code:

   1. Abrir la vista *Extensions*
   2. Pulsar `...`
   3. Elegir *Install from VSIX...*
   4. Seleccionar el fichero `.vsix` generado

## Modos de conexión del Language Server

La extensión ofrece dos modos de conexión:

### `TCP Connection`

La extensión se conecta a un servidor de lenguaje ya ejecutándose. Configuración relevante:

- `wakamiti.languageServer.connectionMode = TCP Connection`
- `wakamiti.languageServer.TCPConnection = localhost:8090`

Este modo encaja con un servidor remoto o con `wakamiti-server-quarkus`.

### `Java Process`

La extensión lanza un proceso Java local para el lenguaje. Configuración relevante:

- `wakamiti.languageServer.connectionMode = Java Process`
- `wakamiti.languageServer.javaProcessPluginPath = <ruta a directorio de plugins>`

Usa este modo cuando no quieres depender de un servidor TCP externo.

## Configuración del servidor de ejecuciones

La vista de ejecución usa un servidor HTTP configurado mediante:

- `wakamiti.executionServer.URL`
- `wakamiti.executionServer.sharedWorkspace`

Por defecto la URL es `http://localhost:8080`.

Si `sharedWorkspace` está activado, el servidor leerá el workspace directamente desde disco. Si está desactivado, la extensión enviará una copia del contenido por HTTP.

## Qué aporta la extensión

Con el manifiesto actual, la extensión registra:

- lenguaje `wakamiti-gherkin` para archivos `.feature`
- reconexión manual al language server
- vista lateral con:
  - `Overview`
  - `Current Execution`
  - `Execution History`
- acción `Run Test Plan` tanto en el editor como en la vista lateral

## Asociación del lenguaje

Si otra extensión está capturando los `.feature`, cambia la asociación del archivo a `Wakamiti Gherkin` desde la barra de estado de VS Code o con *Configure File Association*.
