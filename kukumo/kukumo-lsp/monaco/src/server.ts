/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2018 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
//import * as ws from "ws";
import * as http from "http";
// import * as url from "url";
import * as net from "net";
import * as express from "express";
//import * as rpc from "vscode-ws-jsonrpc";
//import { launch } from "./json-server-launcher";
import { launch2 } from "./tcp-server-launcher";
//import { ConsoleLogger } from "vscode-ws-jsonrpc";

process.on('uncaughtException', function (err: any) {
    console.error('Uncaught Exception: ', err.toString());
    if (err.stack) {
        console.error(err.stack);
    }
});

// create the express application
const app = express();
// server the static content, i.e. index.html
app.use(express.static(__dirname));
// start the http server
const httpServer = app.listen(3000);

/*
// create the web socket
const wss = new ws.Server({
    noServer: true,
    perMessageDeflate: false
});
*/



httpServer.on('upgrade', (request: http.IncomingMessage, socket: net.Socket, head: Buffer) => {
        /*
        // create a tcp socket 
        console.log('Connecting to language server at http://localhost:44444...');
        try {
            const languageServerSocket = net.createConnection(
                { port: 44444 }, 
                () => { 
                    console.log('Connected to language server.')
                    launch2(languageServerSocket);
                }
            );
            languageServerSocket.on('timeout', () => console.error("Timeout connecting to language server"));
        } catch (error) {
            console.error('Error connecting to language server', error);
        }
        */
        /*
        wss.handleUpgrade(request, socket, head, webSocket => {
            const socket: rpc.IWebSocket = {
                send: content => webSocket.send(content, error => {
                    if (error) {
                        throw error;
                    }
                }),
                onMessage: cb => webSocket.on('message', cb),
                onError: cb => webSocket.on('error', cb),
                onClose: cb => webSocket.on('close', cb),
                dispose: () => webSocket.close()
            };
            // launch the server when the web socket is opened
            if (webSocket.readyState === webSocket.OPEN) {
                launch(socket);
            } else {
                webSocket.on('open', () => launch(socket));
            }
        });
        */
})