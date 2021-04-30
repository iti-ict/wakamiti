import * as vscode from 'vscode';
import * as executionServer from '../execution-server';
import { PlanNodeSnapshot } from '../model/PlanNodeSnapshot';
import * as resources from '../shared/resources';


export class PlanNodeTreeItem extends vscode.TreeItem {

    constructor(public node: PlanNodeSnapshot) {
        super(displayName(node) ?? '', collapsibleState(node));
        this.description = description(node);
        this.iconPath = resources.images.iconByNodeType(node, __filename);
    }

}

function collapsibleState(node: PlanNodeSnapshot): vscode.TreeItemCollapsibleState {
    if (node.children === undefined || node.children.length === 0) {
        return vscode.TreeItemCollapsibleState.None;
    } else if (node.nodeType === 'AGGREGATOR') {
        return vscode.TreeItemCollapsibleState.Expanded;
    } else {
        return vscode.TreeItemCollapsibleState.Collapsed;
    }
}


function displayName(node: PlanNodeSnapshot): string | vscode.TreeItemLabel | undefined {
    if (node.nodeType === 'STEP_AGGREGATOR' || node.nodeType === 'STEP' || node.nodeType === 'VIRTUAL_STEP' ) {
        return `${node.keyword ?? ''} ${node.name ?? ''}`;
    } else if (node.nodeType === 'TEST_CASE' && node.id) {
        return {
            label: `${node.id} ${node.name}`,
            highlights: [  [0, node.id.length]]
        };
    } else if (node.name) {
        return node.name;
    } else {
        return node.displayName;
    }
    
}

function description(node: PlanNodeSnapshot): string | undefined {
    if (node.nodeType === 'STEP_AGGREGATOR' || node.nodeType === 'STEP' || node.nodeType === 'VIRTUAL_STEP') {
        return undefined;
    } else {
        return node.keyword ?? '';
    }
    
}
