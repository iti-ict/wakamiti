import * as vscode from 'vscode';
import * as executionServer from '../execution-server';
import { KukumoLanguageClient } from '../language-client';
import { Execution } from '../model/Execution';
import { PlanNodeSnapshot } from '../model/PlanNodeSnapshot';
import * as resources from '../shared/resources';
import { PlanNodeTreeItem } from './plan-node-tree-item';


export class CurrentExecutionView {
    
    public static readonly viewID = 'kukumo.views.executions.current';

    

    readonly dataProvider = new DataProvider();
    readonly treeView = vscode.window.createTreeView(
        CurrentExecutionView.viewID,
        { treeDataProvider: this.dataProvider }
    );
  

    constructor(context: vscode.ExtensionContext) {
        context.subscriptions.push( 
            this.treeView
        );
    }

    public refresh(executionID: string | null = null) {
        if (executionID) {
            this.dataProvider.executionID = executionID;
        }
        this.dataProvider.eventEmitter.fire();
   }


}


class DataProvider implements vscode.TreeDataProvider<PlanNodeSnapshot> {

    readonly eventEmitter = new vscode.EventEmitter<PlanNodeSnapshot | undefined| void>();
    readonly onDidChangeTreeData = this.eventEmitter.event;
    executionID: string | null = null;

    getTreeItem(node: PlanNodeSnapshot): vscode.TreeItem | Thenable<vscode.TreeItem> {
        return new ExecutablePlanNodeTreeItem(node as PlanNodeSnapshot);
    }

    getChildren(node?: PlanNodeSnapshot): vscode.ProviderResult<PlanNodeSnapshot[]> {
        if (node === undefined) {
            if (this.executionID) {
                return executionServer
                    .retrieveExecution(this.executionID)
                    .then( execution => [execution.data!!] );
            } else {
                return null;
            }
        } else {
            return (node as PlanNodeSnapshot)?.children;
        }
    }
}





class ExecutablePlanNodeTreeItem extends PlanNodeTreeItem {

    constructor(public node: PlanNodeSnapshot) {
        super(node);
        this.collapsibleState = collapsibleState(node);
        this.iconPath = resources.images.iconByNodeTypeAndResult(node, __filename);
        this.description = `${this.description ?? ''} | ${node.result ?? 'running...'}`;
    }

}



function collapsibleState(node: PlanNodeSnapshot): vscode.TreeItemCollapsibleState {
    if (node.children === undefined || node.children.length === 0) {
        return vscode.TreeItemCollapsibleState.None;
    } else if (node.result && node.result ===  'PASSED') {
        return vscode.TreeItemCollapsibleState.Collapsed;
    } else {
        return vscode.TreeItemCollapsibleState.Expanded;
    }
}
