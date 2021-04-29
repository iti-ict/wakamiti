import * as vscode from 'vscode';
import * as executionServer from '../execution-server';
import { Execution } from '../model/Execution';
import { PlanNodeSnapshot } from '../model/PlanNodeSnapshot';
import { ExecutionTreeDataProvider } from '../shared/execution-tree-data-provider';
import { PlanNodeTreeDataProvider } from '../shared/plan-node-tree-data-provider';
import * as resources from '../shared/resources';


export class ExecutionsView {
    
    public static readonly viewID = 'kukumo.views.executions';

    readonly dataProvider = new DataProvider();

    constructor(context: vscode.ExtensionContext) {
        context.subscriptions.push( 
            vscode.window.createTreeView(
                ExecutionsView.viewID,
                { treeDataProvider: this.dataProvider }
            )
        );
    }

    public refresh() {
        this.dataProvider.eventEmitter.fire();
    }

    public runTestPlan() {
        executionServer.launchExecution()
        .then( () => this.refresh() );
    }

}


class DataProvider implements vscode.TreeDataProvider<Execution | PlanNodeSnapshot> {

    readonly eventEmitter = new vscode.EventEmitter<Execution | PlanNodeSnapshot | undefined| void>();
    readonly onDidChangeTreeData = this.eventEmitter.event;
    

    getTreeItem(node: Execution | PlanNodeSnapshot): vscode.TreeItem | Thenable<vscode.TreeItem> {
        if (isExecution(node)) {
            return new ExecutionTreeItem(node as Execution);
        } else {
            return new PlanNodeTreeItem(node as PlanNodeSnapshot);
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
    return '["executionID","instant","data"]' === JSON.stringify(Object.keys(object));
}



class ExecutionTreeItem extends vscode.TreeItem {

    constructor(public node: Execution) {
        super(node.data?.displayName ?? '', vscode.TreeItemCollapsibleState.Collapsed);
        this.description = node.instant;
        this.id = node.executionID;
        if (node.data?.result) {
            if (node.data.result === 'PASSED') {
                this.iconPath = resources.images.passed(__filename);
            } else {
                this.iconPath = resources.images.error(__filename);
            }
        } else {
            this.iconPath = resources.images.pending(__filename);
        }
        
        
    }

}

class PlanNodeTreeItem extends vscode.TreeItem {

    constructor(public node: PlanNodeSnapshot) {
        super(node.displayName ?? '', collapsibleState(node));
        this.description = node.nodeType ?? false;
        this.id = `${node.executionID}/${node.id}`;
        if (node.result) {
            if (node.result === 'PASSED') {
                this.iconPath = resources.images.passed(__filename);
            } else {
                this.iconPath = resources.images.error(__filename);
            }
        } else {
            this.iconPath = resources.images.pending(__filename);
        }
    }

}



function collapsibleState(node: PlanNodeSnapshot): vscode.TreeItemCollapsibleState {
    if (node.children === undefined || node.children.length === 0) {
        return vscode.TreeItemCollapsibleState.None;
    } else if (node.nodeType === 'TEST_CASE') {
        return vscode.TreeItemCollapsibleState.Collapsed;
    } else {
        return vscode.TreeItemCollapsibleState.Expanded;
    }
}
