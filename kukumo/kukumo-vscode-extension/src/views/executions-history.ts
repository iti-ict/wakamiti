import * as vscode from 'vscode';
import * as executionServer from '../execution-server';
import { KukumoLanguageClient } from '../language-client';
import { Execution } from '../model/Execution';
import { PlanNodeSnapshot } from '../model/PlanNodeSnapshot';
import * as resources from '../shared/resources';
import { PlanNodeTreeItem } from './plan-node-tree-item';


export class ExecutionHistoryView {
    
    public static readonly viewID = 'kukumo.views.executions.history';

    readonly dataProvider = new DataProvider();

    constructor(context: vscode.ExtensionContext) {
        context.subscriptions.push( 
            vscode.window.createTreeView(
                ExecutionHistoryView.viewID,
                { treeDataProvider: this.dataProvider }
            )
        );
    }

    public refresh() {
        this.dataProvider.eventEmitter.fire();
    }

}


class DataProvider implements vscode.TreeDataProvider<Execution | PlanNodeSnapshot> {

    readonly eventEmitter = new vscode.EventEmitter<Execution | PlanNodeSnapshot | undefined| void>();
    readonly onDidChangeTreeData = this.eventEmitter.event;
    

    getTreeItem(node: Execution | PlanNodeSnapshot): vscode.TreeItem | Thenable<vscode.TreeItem> {
        if (isExecution(node)) {
            return new ExecutionTreeItem(node as Execution);
        } else {
            return new ExecutedPlanNodeTreeItem(node as PlanNodeSnapshot);
        }
    }

    getChildren(node?: Execution | PlanNodeSnapshot): vscode.ProviderResult<Execution[] | PlanNodeSnapshot[]> {
        if (node === undefined) {
            return executionServer.retrieveExecutions();
        } else if (isExecution(node) && node.executionID) {
            return executionServer.retrieveExecution(node.executionID).then(execution => execution.data?.children);
        } else {
            return (node as PlanNodeSnapshot)?.children;
        }
    }
}




function isExecution(object: Execution | PlanNodeSnapshot): boolean {
    const keys = Object.keys(object);
    return keys.includes('executionID') && keys.includes('executionInstant');
}



class ExecutionTreeItem extends vscode.TreeItem {

    constructor(public node: Execution) {
        super(node.data?.displayName ?? '', vscode.TreeItemCollapsibleState.Collapsed);
        this.description = node.executionInstant;
        this.iconPath = resources.images.iconByNodeTypeAndResult(node.data, __filename);     
    }

}

class ExecutedPlanNodeTreeItem extends PlanNodeTreeItem {

    constructor(public node: PlanNodeSnapshot) {
        super(node);
        this.collapsibleState = collapsibleState(node);
        this.iconPath = resources.images.iconByNodeTypeAndResult(node, __filename);
        if (node.nodeType === 'STEP' && node.errorMessage) {
            this.tooltip = node.errorMessage;
            this.contextValue = 'stepWithError';
        }
        this.description = `${this.description ?? ''} | ${node.result}`;
    }

}



function collapsibleState(node: PlanNodeSnapshot): vscode.TreeItemCollapsibleState {
    if (node.children === undefined || node.children.length === 0) {
        return vscode.TreeItemCollapsibleState.None;
    } else if (node.result && (node.result ===  'PASSED' || node.result === 'SKIPPED')) {
        return vscode.TreeItemCollapsibleState.Collapsed;
    } else {
        return vscode.TreeItemCollapsibleState.Expanded;
    }
}