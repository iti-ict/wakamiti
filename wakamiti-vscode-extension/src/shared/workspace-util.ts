/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as vscode from 'vscode';
import * as fs from 'fs';

interface FileContent {
    path: string;
    content: string;
}

export type WorkspaceContents = { [path: string]: string };

export function readWorkspaceContents(): Promise<WorkspaceContents | string> {

    return Promise.all([
        vscode.workspace.findFiles('wakamiti.yaml'),
        vscode.workspace.findFiles('**/*.feature')
    ])
    .then( files => files[0].concat(files[1]) )
    .then( files => files.map( uri => readPath(uri)))
    .then( fileContents => {
        if (fileContents.length === 1 && fileContents[0].path.endsWith('.feature')) {
            return fileContents[0].content;
        } else {
            const workspaceContents: WorkspaceContents = {};
            fileContents.forEach( fileContent => workspaceContents[fileContent.path] = fileContent.content );
            return workspaceContents;
        }
    } );
    
    
    
}

function readPath (uri: vscode.Uri): FileContent {
    const workspacePath = vscode.workspace.getWorkspaceFolder(uri)?.uri.path;
    const relativePath = (workspacePath && uri.path.substring(workspacePath?.length)) ?? uri.path;
    return { path: relativePath.substring(1), content: fs.readFileSync (uri.path, 'utf8') }
}