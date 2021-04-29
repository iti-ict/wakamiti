import * as vscode from 'vscode';
import { KukumoLanguageClient } from './language-client';
import { ExecutionsView } from './views/executions';
import { PlanOverviewView } from './views/overview';


var executionsView: ExecutionsView;
var planOverviewView: PlanOverviewView;
var languageClient: KukumoLanguageClient;



export function activate(context: vscode.ExtensionContext) {
	console.log('Activating Kukumo VSCode extension...');
	languageClient = new KukumoLanguageClient(context, reconnectLanguageServer.id);
	planOverviewView = new PlanOverviewView(context);
	executionsView = new ExecutionsView(context);
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
		refreshExecutions,
		runTestPlan
	]
	.forEach( it => context.subscriptions.push(
			vscode.commands.registerCommand(it.id, it.action)
		)
	);

}


interface Command {
	id: string;
	action: () => void
}

export const reconnectLanguageServer: Command = {
    id: 'kukumo.commands.reconnectLanguageServer',
    action: () => languageClient.start()
};

export const refreshPlanOverview: Command = {
    id: 'kukumo.commands.planOverview.refresh',
    action: () => planOverviewView.refresh()
};

export const refreshExecutions: Command = {
    id: 'kukumo.commands.executions.refresh',
    action: () =>  executionsView.refresh()
};

export const runTestPlan: Command = {
    id: 'kukumo.commands.executions.run',
    action: () => executionsView.runTestPlan()
};

