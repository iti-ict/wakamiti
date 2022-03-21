/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as settings from './shared/settings';
import * as http from './shared/http-client';
import * as vscode from 'vscode';
import { PropertyError } from './shared/property-error';
import { PlanNodeSnapshot } from './model/PlanNodeSnapshot';
import * as fs from 'fs';
import { readWorkspaceContents } from './shared/workspace-util';
import { Execution } from './model/Execution';
import jwt_decode from "jwt-decode";


var nextTokenExpiration : number | undefined = undefined;
var token : string | undefined = undefined;



export function requestAnalyze(): Promise<PlanNodeSnapshot> {
    if (!vscode.workspace.workspaceFolders) {
        return Promise.reject('There is no workspace opened');
    }
    const localWorkspace = vscode.workspace.getConfiguration().get(settings.EXECUTION_SERVER_SHARED_WORKSPACE, false);
    const { host, port } = getHostAndPort();  
    const workspaceFolder = vscode.workspace.workspaceFolders[0];

    return obtainToken(host,port).then( ()=>{
        if (localWorkspace) {
            return http.post<PlanNodeSnapshot>(
                host,port,`plans?workspace=${workspaceFolder.uri.path}`);
        } else {
            return readWorkspaceContents().then(workspaceContents => 
                http.post<PlanNodeSnapshot>(host,port,`plans?contentType=gherkin`, workspaceContents)
            );
        }
    });
}


export function launchExecution(): Promise<Execution> {
    if (!vscode.workspace.workspaceFolders) {
        return Promise.reject('There is no workspace opened');
    }
    const localWorkspace = vscode.workspace.getConfiguration().get(settings.EXECUTION_SERVER_SHARED_WORKSPACE, false);
    const { host, port } = getHostAndPort();  
    const workspaceFolder = vscode.workspace.workspaceFolders[0];

    return obtainToken(host,port).then( ()=>{
        if (localWorkspace) {
            return http.post<Execution>(host,port,`executions?async=true&workspace=${workspaceFolder.uri.path}`);
        } else {
            return readWorkspaceContents().then(workspaceContents => 
                http.post<Execution>(host,port,`executions?async=true&contentType=gherkin`, workspaceContents)
            );
        }
    });
}



export function retrieveExecutions(): Promise<Execution[]> {
    const { host, port } = getHostAndPort(); 
    return obtainToken(host,port).then( ()=>{ 
        return http.get<Execution[]>(host,port,`executions`);
    });
}


export function retrieveExecution(executionID: string): Promise<Execution> {
    const { host, port } = getHostAndPort();  
    return obtainToken(host,port).then( ()=>{ 
        return http.get<Execution>(host,port,`executions/${executionID}`);
    });
}




function getHostAndPort() {
    const url: String = vscode.workspace.getConfiguration().get(settings.EXECUTION_SERVER_URL, '');
    if (url === '') {
        throw new PropertyError(settings.EXECUTION_SERVER_URL, 'not configured');
    }
    return {
        host: url.split(':')[0],
        port: parseInt(url.split(':')[1])
    };

}


function obtainToken(host: string, port: number): Promise<void> {
    const requireNewToken = 
        (nextTokenExpiration === undefined) ||
        nextTokenExpiration < new Date().getTime() - 1 * 60000   
    ;
    if (requireNewToken) {
        return new Promise( (resolve,reject) => {
            http.get<string>(host,port,'tokens')
                .then( response => { 
                    token = response;
                    const decoded: any = jwt_decode(token);
                    nextTokenExpiration = decoded.exp * 1000;
                    http.setAuthorization(`Bearer ${response}`); 
                    console.info(`new token with expiration ${nextTokenExpiration} (current date is ${new Date().getTime()})`);                    
                    resolve(); 
                } )
                .catch( error => { 
                    console.info('discard token');
                    token = undefined;
                    http.setAuthorization(undefined);
                    nextTokenExpiration = undefined;
                    reject(error); 
                });
        });
    } else {
        return Promise.resolve();
    }
}