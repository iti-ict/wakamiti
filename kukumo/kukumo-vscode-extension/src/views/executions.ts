import * as vscode from 'vscode';
import * as executionServer from '../execution-server-client';
import { ExecutionTreeDataProvider } from '../shared/execution-tree-data-provider';
import { PlanNodeTreeDataProvider } from '../shared/plan-node-tree-data-provider';


const executionsDataProvider = new ExecutionTreeDataProvider();

export function prepareExecutionsView(context: vscode.ExtensionContext) {

    context.subscriptions.push(
        vscode.commands.registerCommand(
			'kukumo.executions.run',
			() => launchExecution()
	    ),
        vscode.commands.registerCommand(
			'kukumo.executions.refresh',
			() => executionsDataProvider.refresh()
	    ),
        vscode.window.createTreeView(
            'kukumo-executions',
            { treeDataProvider: executionsDataProvider }
        )
    );
    executionsDataProvider.refresh();

    
}

function launchExecution() {
    executionServer.launchExecution().then(()=>executionsDataProvider.refresh());
}

