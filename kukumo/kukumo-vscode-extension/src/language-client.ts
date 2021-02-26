import * as vscode from 'vscode';
import * as net from 'net';
import { 
    CloseAction, 
    ErrorAction, 
    LanguageClient, 
    LanguageClientOptions, 
    Message, 
    StreamInfo 
} from 'vscode-languageclient/node';


const LANGUAGE_ID = 'kukumo-gherkin';
const PROPERTIES_SECTION = 'kukumo.languageServer';
const PROPERTY_CONNECTION_MODE = 'kukumo.languageServer.connectionMode';
const PROPERTY_TCP_CONNECTION = 'kukumo.languageServer.TCPConnection';
const PROPERTY_JAVA_PLUGIN_PATH = 'kukumo.languageServer.javaProcessPluginPath';

const CONNECTION_MODE = {
    tcp: "TCP Connection",
    java: "Java Process"
};


const LANGUAGE_CLIENT_OPTIONS : LanguageClientOptions = {
    documentSelector: [{
        scheme: 'file',
        language: LANGUAGE_ID
    }],
    initializationFailedHandler: onLanguageClientInitializacionFailed,
    errorHandler: {
        error: onLanguageClientError,
        closed: onLanguageClientClosed
    },
    synchronize: {
        fileEvents: vscode.workspace.createFileSystemWatcher('*')
    },
    progressOnInitialization: true
};


export function startLanguageClient(context: vscode.ExtensionContext) {
    context.subscriptions.push( listenPropertiesChanges(context) );
    const connectionMode = vscode.workspace.getConfiguration().get(PROPERTY_CONNECTION_MODE, '');
    if (connectionMode === '') {
        throw new Error(`Property ${PROPERTY_CONNECTION_MODE} not configured`);
    }
    if (connectionMode === CONNECTION_MODE.tcp) {
        startTcpLanguageClient(context);
    }
}



function startTcpLanguageClient(context: vscode.ExtensionContext): vscode.Disposable {
    const tcpServer :string = vscode.workspace.getConfiguration().get(PROPERTY_TCP_CONNECTION, '');
    if (tcpServer === '') {
        throw new Error(`Property ${PROPERTY_TCP_CONNECTION} not configured`);
    }
    const [host, port] = tcpServer.split(':', 2);
    const client = new LanguageClient(
        'kukumo-languange-client', 
        'Kukumo Language Server Client', 
        () => tcpServerProvider(host, parseInt(port)), 
        LANGUAGE_CLIENT_OPTIONS
    );
    // TODO: reconnect on error
    console.log("Starting Kukumo language client...");
    client.onReady().then(() => console.log('Kukumo language client ready'));
    return client.start();
}




function listenPropertiesChanges(context: vscode.ExtensionContext): vscode.Disposable {
    return vscode.workspace.onDidChangeConfiguration(event => {
        if (!event.affectsConfiguration(PROPERTIES_SECTION)) {
            return;
        }
        if (event.affectsConfiguration(PROPERTY_CONNECTION_MODE)) {
            console.log('Kukumo language server connection mode has change, restarting language client...');
        }
    });
}





function tcpServerProvider(host: string, port: number) : Promise<StreamInfo> {
    return new Promise((resolve, reject) => {
        try {
            console.log(`Connecting to Kukumo TCP language server ${host}:${port} ...`);
            var socketClient = new net.Socket();
            socketClient.connect(
                port, 
                host, 
                () => console.info('Connected to Kukumix language server.')
            );
            const streamInfo = {
                writer: socketClient,
                reader: socketClient,
                detached: false
            };
            resolve(streamInfo);
        }
        catch (error) {
            console.error(error);
            reject(error);
        }
    });
}


function onLanguageClientInitializacionFailed(error: any) {
    console.error('Error initializing Kukumo language client', error);
    return false;
}


function onLanguageClientError(error: Error, message: Message, count: number) {
    vscode.window.showErrorMessage(`Cannot activate Kukumo language client: ${error.message}`);
    console.log(error, count);
    return ErrorAction.Shutdown;
}


function onLanguageClientClosed() {
    console.log('Kukumo language client closed.');
    return CloseAction.DoNotRestart;
}
