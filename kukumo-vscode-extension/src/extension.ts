/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as vscode from 'vscode';
import { KukumoLanguageClient } from './language-client';
import { PlanNodeSnapshot } from './model/PlanNodeSnapshot';
import { ExecutionHistoryView } from './views/executions-history';
import { PlanOverviewView } from './views/overview';
import * as executionServer from './execution-server';
import { CurrentExecutionView } from './views/current-execution';
import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';


var executionHistoryView: ExecutionHistoryView;
var planOverviewView: PlanOverviewView;
var currentExecutionView: CurrentExecutionView;
var languageClient: KukumoLanguageClient;



export function activate(context: vscode.ExtensionContext) {
	console.log('Activating Kukumo VSCode extension...');
	languageClient = new KukumoLanguageClient(context, reconnectLanguageServer.id);
	planOverviewView = new PlanOverviewView(context);
	executionHistoryView = new ExecutionHistoryView(context);
	currentExecutionView = new CurrentExecutionView(context);
	registerCommands(context);
	configureOtherExtensions();
	console.log('Kukumo VSCode extension activated.');
}




export function deactivate() {
	console.log('Kukumo VSCode extension deactivated.');
}






function registerCommands(context: vscode.ExtensionContext) {
	[
		reconnectLanguageServer,
		refreshPlanOverview,
		refreshExecutionHistory,
		runTestPlan,
		showExecutionError,
		refreshCurrentExecution,
		showStepDocument,
		showStepDataTable
	]
	.forEach( it => context.subscriptions.push(
			vscode.commands.registerCommand(it.id, it.action)
		)
	);



}


interface Command {
	id: string;
	action: (...args: any[]) => any
}

export const reconnectLanguageServer: Command = {
    id: 'kukumo.commands.reconnectLanguageServer',
    action: () => languageClient.start()
};

export const refreshPlanOverview: Command = {
    id: 'kukumo.commands.planOverview.refresh',
    action: () => planOverviewView.refresh()
};

export const refreshExecutionHistory: Command = {
    id: 'kukumo.commands.executions.history.refresh',
    action: () =>  executionHistoryView.refresh()
};

export const refreshCurrentExecution: Command = {
    id: 'kukumo.commands.executions.current.refresh',
    action: () =>  currentExecutionView.refresh()
};


export const runTestPlan: Command = {
    id: 'kukumo.commands.executions.run',
    action: () => executionServer
		.launchExecution()
		.then(execution => currentExecutionView.refresh(execution.executionID!!))
};


export const showExecutionError: Command = {
	id: 'kukumo.commands.executions.showError',
	action: (node: PlanNodeSnapshot) => {
		vscode.workspace.openTextDocument(
			{
				content: node.errorMessage + '\n\n' + node.errorTrace
			}
		).then( textDocument => vscode.window.showTextDocument(textDocument) );
	}
};

export const showStepDocument: Command = {
	id: 'kukumo.commands.executions.showStepDocument',
	action: (node: PlanNodeSnapshot) => {
		const file = tempFile( 
			node.documentType ? `document.${node.documentType}` : 'document' , 
			node.document
		);
		vscode.workspace.openTextDocument(file)
		.then( textDocument => vscode.window.showTextDocument(textDocument, vscode.ViewColumn.Beside, true) );
	}
};


export const showStepDataTable: Command = {
	id: 'kukumo.commands.executions.showStepDataTable',
	action: (node: PlanNodeSnapshot) => {
		const csv = node.dataTable?.map( row =>	row.map( it => `"${it}"`).join(',')).join('\n');
		const csvFile = tempFile('datatable', csv);
		vscode.commands.executeCommand('csv.preview', csvFile);
	}
};


function tempFile (name: string, data: string = '', encoding: string = 'utf8'): vscode.Uri {
	const tempFolder = fs.mkdtempSync( path.join(os.tmpdir(), 'kukumo-') );
	const tempFile = path.join( tempFolder, name );
	fs.writeFileSync(tempFile,data,encoding);
	return vscode.Uri.parse(tempFile);
}


function configureOtherExtensions() {
	const settings = vscode.workspace.getConfiguration();
	settings.update('csv-preview.resizeColumns', 'all');
	settings.update('csv-preview.formatValues','never');
	settings.update('csv-preview.capitalizeHeaders', false);
	settings.update('csv-preview.separator',',');
	settings.update('csv-preview.quoteMark', '"');
	settings.update('csv-preview.hasHeaders', true);
}