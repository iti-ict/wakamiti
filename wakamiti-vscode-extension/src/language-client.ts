/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as vscode from 'vscode';
import * as net from 'net';
import { 
    CloseAction, 
    ErrorAction, 
    LanguageClient, 
    LanguageClientOptions, 
    Message, 
    StreamInfo 
} from 'vscode-languageclient';
import * as cp from 'child_process';
import * as path from 'path';
import * as fs from 'fs';
import { PropertyError } from './shared/property-error';
import * as settings from './shared/settings';



const LANGUAGE_ID = 'wakamiti-gherkin';


const CONNECTION_MODE = {
    tcp: "TCP Connection",
    java: "Java Process"
};


const LANGUAGE_CLIENT_OPTIONS : LanguageClientOptions = {
    documentSelector: [
        {
            scheme: 'file',
            language: LANGUAGE_ID
        },
        {
            scheme: 'file',
            language: 'yaml'
        }
    ],
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

const STATUS_BAR_TEXT_OFFLINE = 'KLS ⮔';
const STATUS_BAR_TEXT_ONLINE = 'KLS ⭾';
const STATUS_BAR_TOOLTIP_OFFLINE = 'Not connected with Wakamiti Language Server, press to re-try';
const STATUS_BAR_TOOLTIP_ONLINE = 'Connected with Wakamiti Language Server';

var client: LanguageClient;



export class WakamitiLanguageClient {

    readonly context: vscode.ExtensionContext;
    readonly connectionStatus: vscode.StatusBarItem;
    client: LanguageClient | null = null;

    constructor(context: vscode.ExtensionContext, reconnectCommand: string) {    
        this.context = context;
        this.connectionStatus = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Right);
        this.connectionStatus.text = STATUS_BAR_TEXT_OFFLINE;
        this.connectionStatus.command = reconnectCommand;
        this.connectionStatus.tooltip = STATUS_BAR_TOOLTIP_OFFLINE;
        this.connectionStatus.show();
        this.listenPropertiesChanges();
        this.start();
    }



    public start() {
        if (client) {
            client.stop();
        }
        try {
            const connectionMode = vscode.workspace.getConfiguration().get(settings.PROPERTY_CONNECTION_MODE, '');
            if (connectionMode === '') {
                throw new PropertyError(settings.PROPERTY_CONNECTION_MODE, 'not configured');
            } else if (connectionMode === CONNECTION_MODE.tcp) {
                this.startTcpLanguageClient();
            } else if (connectionMode === CONNECTION_MODE.java) {
                this.startJavaProcessLanguageCLiente();
            }
        } catch (error) {
            if (error instanceof PropertyError) {
                error.showError();
            } else {
                vscode.window.showErrorMessage(`${error}`);
            }
        }
    }


    private startTcpLanguageClient() {
        const tcpServer :string = vscode.workspace.getConfiguration().get(settings.PROPERTY_TCP_CONNECTION, '');
        if (tcpServer === '') {
            throw new PropertyError(settings.PROPERTY_TCP_CONNECTION, 'not configured');
        }
        const [host, port] = tcpServer.split(':', 2);
    
        const languageClient = new LanguageClient(
            'wakamiti-languange-client',
            'Wakamiti Language Server Client',
            () => this.tcpServerProvider(host, parseInt(port)),
            LANGUAGE_CLIENT_OPTIONS
        );
        this.launchLanguageClient(languageClient);
    }
    



    private startJavaProcessLanguageCLiente() {

        const pluginsPath :string = vscode.workspace.getConfiguration().get(settings.PROPERTY_JAVA_PLUGIN_PATH, '');
        if (pluginsPath === '') {
            throw new PropertyError(settings.PROPERTY_JAVA_PLUGIN_PATH, 'not configured');
        }
        if (!fs.existsSync(pluginsPath)) {
            throw new PropertyError(settings.PROPERTY_JAVA_PLUGIN_PATH, 'path does not exist');
        }
        
        const languageClient = new LanguageClient(
            'wakamiti-languange-client',
            'Wakamiti Language Server Client',
            () => runLanguageServerAsJavaProcess(pluginsPath),
            LANGUAGE_CLIENT_OPTIONS
        );
        // languageClient.handleFailedRequest = ( (type, error, defaultValue)=> {
        //     console.log('Failed request',type,error,defaultValue);
        //     return defaultValue;
        // });
        this.launchLanguageClient(languageClient);
        
    }


    private launchLanguageClient(newClient: LanguageClient) {
        console.log("Starting Wakamiti language client...");
        newClient.onReady().then(() => {
            console.log('Wakamiti language client ready');
            this.connectionStatus.text = STATUS_BAR_TEXT_ONLINE;
            this.connectionStatus.tooltip = STATUS_BAR_TOOLTIP_ONLINE;
        });
        this.context.subscriptions.push( 
            newClient.start() 
        );
        this.client = newClient;
    }



    private listenPropertiesChanges() {
        this.context.subscriptions.push(
            vscode.workspace.onDidChangeConfiguration(event => {
                if (!event.affectsConfiguration(settings.PROPERTIES_SECTION)) {
                    return;
                }
                console.log(`Property changed!`,event);
                if (event.affectsConfiguration(settings.PROPERTY_CONNECTION_MODE)) {
                    console.log('Wakamiti language server connection mode has change, restarting language client...');
                    if (client) {
                        client.stop().then(()=>this.start());
                    }
                }
            })
        );
    }



    private tcpServerProvider(host: string, port: number) : Promise<StreamInfo> {
        return new Promise((resolve, reject) => {
            try {
                console.log(`Connecting to Wakamiti TCP language server ${host}:${port} ...`);
                var socketClient = new net.Socket();
    
                socketClient.addListener('error', error => this.notifyConnectionLost(error));
                socketClient.addListener('timeout', () => this.notifyConnectionLost());
                socketClient.addListener('close', () => this.notifyConnectionLost());
    
                socketClient.connect(
                    port, 
                    host, 
                    () => console.info('Connected to Wakamiti language server.')
                );
                const streamInfo = {
                    writer: socketClient,
                    reader: socketClient,
                    detached: false
                };
                resolve(streamInfo);
            }
            catch (error) {
                console.error('Error connecting to Wakamiti TCP language server', error);
                reject(error);
            }
        });
    }


    private notifyConnectionLost(error? : Error) {
        console.log('Connection closed', error);
        this.connectionStatus.text = STATUS_BAR_TEXT_OFFLINE;
        this.connectionStatus.tooltip = STATUS_BAR_TOOLTIP_OFFLINE;
        this.client?.stop();
        vscode.window.showWarningMessage(
            'Connection with language server closed',
            'Reconnect'
        ).then( action => {
            if (action === 'Reconnect' ) {
                this.start();
            } 
        });
    }
    


}







function onLanguageClientInitializacionFailed(error: any) {
    console.error('Error initializing Wakamiti language client', error);
    return false;
}


function onLanguageClientError(error: Error, message: Message, count: number) {
    vscode.window.showErrorMessage(`Cannot activate Wakamiti language client: ${error.message}`);
    console.log(error, count);
    return ErrorAction.Shutdown;
}


function onLanguageClientClosed() {
    console.log('Wakamiti language client closed.');
    return CloseAction.DoNotRestart;
}



function runLanguageServerAsJavaProcess(pluginPath: string): Promise<cp.ChildProcess> {
    return new Promise((resolve, reject) => {
        try {
            const libPath = path.join(__dirname, '..', 'lib');
            const command = `java -p ${libPath}:${pluginPath} -m wakamiti.lsp/Launcher -d`;
            console.log(command);
            const process = cp.exec(command, (error, stdout, stderr) => console.log(stderr));
            process.addListener('close', (code) => console.log(`Java process close with code ${code}`));
            process.addListener('disconnect', () => console.log(`Java process disconnected`));
            process.addListener('error', (error) => console.log(`Java process error ${error}`));
            process.addListener('exit', () => console.log(`Java process exited`));
            process.addListener('message', (message) => console.log(`Java process message ${message}`));
            resolve(process);
        } catch (error) {
            reject(error);
        }
    });
}