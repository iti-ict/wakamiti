import * as vscode from 'vscode';
import { KukumoLanguageClient } from './language-client';
import { PlanNodeSnapshot } from './model/PlanNodeSnapshot';
import { ExecutionHistoryView } from './views/executions-history';
import { PlanOverviewView } from './views/overview';
import * as executionServer from './execution-server'import { CurrentExecutionView } from './views/current-execution';
''


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
		refreshCurrentExecution
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

