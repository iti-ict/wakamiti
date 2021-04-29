import * as settings from './shared/settings';
import * as http from './shared/http-client';
import * as vscode from 'vscode';
import { PropertyError } from './shared/property-error';
import { PlanNodeSnapshot } from './model/PlanNodeSnapshot';
import * as fs from 'fs';
import { readWorkspaceContents } from './shared/workspace-util';
import { Execution } from './model/Execution';


export function requestAnalyze(): Promise<PlanNodeSnapshot> {
    if (!vscode.workspace.workspaceFolders) {
        return Promise.reject('There is no workspace opened');
    }
    const localWorkspace = vscode.workspace.getConfiguration().get(settings.EXECUTION_SERVER_SHARED_WORKSPACE, false);
    const { host, port } = getHostAndPort();  
    const workspaceFolder = vscode.workspace.workspaceFolders[0];

    if (localWorkspace) {
        return http.post<PlanNodeSnapshot>(host,port,`plans?workspace=${workspaceFolder.uri.path}`);
    } else {
        return readWorkspaceContents().then(workspaceContents => 
            http.post<PlanNodeSnapshot>(host,port,`plans?contentType=gherkin`, workspaceContents)
        );
    }
}


export function launchExecution(): Promise<Execution> {
    if (!vscode.workspace.workspaceFolders) {
        return Promise.reject('There is no workspace opened');
    }
    const localWorkspace = vscode.workspace.getConfiguration().get(settings.EXECUTION_SERVER_SHARED_WORKSPACE, false);
    const { host, port } = getHostAndPort();  
    const workspaceFolder = vscode.workspace.workspaceFolders[0];

    if (localWorkspace) {
        return http.post<Execution>(host,port,`executions?async=true&workspace=${workspaceFolder.uri.path}`);
    } else {
        return readWorkspaceContents().then(workspaceContents => 
            http.post<Execution>(host,port,`executions?async=true&contentType=gherkin`, workspaceContents)
        );
    }
}



export function retrieveExecutions(): Promise<Execution[]> {
    const { host, port } = getHostAndPort();  
    return http.get<Execution[]>(host,port,`executions`);
}


export function retrieveExecution(executionID: string): Promise<Execution> {
    const { host, port } = getHostAndPort();  
    return http.get<Execution>(host,port,`executions/${executionID}`);
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