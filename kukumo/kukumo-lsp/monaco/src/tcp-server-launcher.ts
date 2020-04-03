/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2018 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
import * as path from 'path';
import * as rpc from "vscode-ws-jsonrpc";
import * as server from "vscode-ws-jsonrpc/lib/server";
import * as lsp from "vscode-languageserver";
import * as net from "net";


export function launch2(socket: net.Socket) {
    const reader = new rpc.SocketMessageReader(socket);
    const writer = new rpc.SocketMessageWriter(socket);
    const extJsonServerPath = path.resolve(__dirname, 'ext-json-server.js');
    const socketConnection = server.createConnection(reader, writer, () => socket.destroy() );
    const serverConnection = server.createServerProcess('JSON', 'node', [extJsonServerPath]);
    server.forward(socketConnection, serverConnection, message => {
        if (rpc.isRequestMessage(message)) {
            if (message.method === lsp.InitializeRequest.type.method) {
                const initializeParams = message.params as lsp.InitializeParams;
                initializeParams.processId = process.pid;
            }
        }
        return message;
    });
}
