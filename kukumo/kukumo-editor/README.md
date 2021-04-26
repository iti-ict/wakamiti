## Prerquisites

### Install `yarn`

```shell
sudo npm install -g yarn
```

### Install dependencies
```shell
yarn
```

### Prepare and Build the application
```shell
yarn prepare
```

### Running the application
```shell
yarn start
```

You can provide a workspace path to open as a first argument and `--hostname`, `--port` options to deploy the application on specific network interfaces and ports, e.g. to open `/workspace` on all interfaces and port `8080`:

```shell
yarn start /my-workspace --hostname 0.0.0.0 --port 8080
```


## Testing custom VSCode extensions

For VSCode extensions that are not yet published, the procedure to integrate into 
the editor is the following:

1. Package the VSCode extension using `vsce package`
2. Rename the created file changing the extension from `.vsix` to `.zip`
3. Extract the ZIP file in a folder under the `plugins` folder 
