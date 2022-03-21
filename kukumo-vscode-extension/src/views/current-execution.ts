/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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
                    .then ( execution => checkPendingStatus(execution, ()=> this.eventEmitter.fire() ) )
                    .then( execution => [execution.data!!] );
            } else {
                return null;
            }
        } else {
            return (node as PlanNodeSnapshot)?.children;
        }
    }
}



function checkPendingStatus(execution: Execution, callback: ()=>void): Execution {
    const pending = execution.data ? hasPendingStatus(execution.data) : true;
    if (pending) {
        setTimeout(callback, 1000);
    }
    return execution;
}


function hasPendingStatus(node: PlanNodeSnapshot): boolean {
    if (!node.result) {
        return true;
    } else if (node.children) {
        return node.children.map( child => hasPendingStatus(child) ).reduce( (a,b) => a || b);
    } else {
        return false;
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
    } else if (node.nodeType === 'AGGREGATOR' || (node.result && (node.result ===  'ERROR' || node.result === 'FAILED'))) {
        return vscode.TreeItemCollapsibleState.Expanded;
    } else {
        return vscode.TreeItemCollapsibleState.Collapsed;
    }
}