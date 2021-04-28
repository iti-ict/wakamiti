import * as vscode from 'vscode';
import * as executionServer from '../execution-server-client';
import { PlanNodeTreeDataProvider } from '../shared/plan-node-tree-data-provider';


const overviewDataProvider = new PlanNodeTreeDataProvider();

export function prepareOverviewView(context: vscode.ExtensionContext) {

    context.subscriptions.push(
		vscode.commands.registerCommand(
			'kukumo.overview.refresh',
			() => refreshOverviewView()
	    ),
        vscode.window.createTreeView(
            'kukumo-plan-overview',
            { treeDataProvider: overviewDataProvider }
        )
    );

    refreshOverviewView();
    
}

function refreshOverviewView() {
    executionServer.requestAnalyze()
        .then( plan => overviewDataProvider.refresh(plan))
        .catch( error => vscode.window.showErrorMessage(`${error}`));
}