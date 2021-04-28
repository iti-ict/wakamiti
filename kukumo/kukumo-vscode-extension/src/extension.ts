import * as vscode from 'vscode';
import { start as startLanguageClient } from './language-client';
import { prepareOverviewView } from './views/overview';
import { PlanNodeTreeDataProvider } from './shared/plan-node-tree-data-provider';
import { prepareExecutionsView } from './views/executions';



export function activate(context: vscode.ExtensionContext) {
	console.log('Activating Kukumo VSCode extension...');
	startLanguageClient(context);	
	prepareOverviewView(context);
	prepareExecutionsView(context);
	
	console.log('Kukumo VSCode extension activated.');
}




export function deactivate() {
	console.log('Kukumo VSCode extension deactivated.');
}



