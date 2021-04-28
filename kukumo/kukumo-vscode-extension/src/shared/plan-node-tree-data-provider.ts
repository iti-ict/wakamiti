import * as vscode from 'vscode';
import { PlanNodeSnapshot } from '../model/PlanNodeSnapshot';




export class PlanNodeTreeDataProvider implements vscode.TreeDataProvider<PlanNodeSnapshot> {

    private plan: PlanNodeSnapshot | undefined;
    private eventEmitter = new vscode.EventEmitter<PlanNodeSnapshot | undefined| void>();
    readonly onDidChangeTreeData = this.eventEmitter.event;
    

    getTreeItem(node: PlanNodeSnapshot): vscode.TreeItem | Thenable<vscode.TreeItem> {
        return new PlanNodeTreeItem(node);
    }

    getChildren(node?: PlanNodeSnapshot): vscode.ProviderResult<PlanNodeSnapshot[]> {
        if (node === undefined && this.plan !== undefined) {
            return [this.plan];
        } else {
            return node?.children;
        }
    }


    refresh(node?: PlanNodeSnapshot) {
        this.plan = node;
        this.eventEmitter.fire();
    }

}


class PlanNodeTreeItem extends vscode.TreeItem {

    constructor(public node: PlanNodeSnapshot) {
        super(node.displayName ?? '', collapsibleState(node));
        this.description = node.nodeType ?? false;
        this.id = node.id;
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