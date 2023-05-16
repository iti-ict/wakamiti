/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as vscode from 'vscode';
import * as executionServer from '../execution-server';
import { PlanNodeSnapshot } from '../model/PlanNodeSnapshot';
import * as resources from '../shared/resources';
import { PlanNodeTreeItem } from './plan-node-tree-item';


export class PlanOverviewView {

    public static readonly viewID = 'wakamiti.views.planOverview';

    readonly dataProvider = new DataProvider();
    
    constructor(context: vscode.ExtensionContext) {
        context.subscriptions.push( 
            vscode.window.createTreeView(
                PlanOverviewView.viewID,
                { treeDataProvider: this.dataProvider }
            )
        );
        this.refresh();
    }

    
    refresh() {
        executionServer.requestAnalyze()
        .then( plan => this.dataProvider.refresh(plan))
        .catch( error => vscode.window.showErrorMessage(`${error}`));
    }
    
}




class DataProvider implements vscode.TreeDataProvider<PlanNodeSnapshot> {

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