/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as vscode from 'vscode';

export class PropertyError {

    property: string;
    message: string;

    constructor (property: string, message: string) {
        this.property = property;
        this.message = message;
    }    

    showError() {
        vscode.window.showErrorMessage(
            `Value of property ${this.property} is not valid: ${this.message}`, 
            'Change property value'
        ).then( action => this.openPreferences() );
    }
    
    private openPreferences() {
        vscode.commands.executeCommand( 'workbench.action.openSettings', this.property );
    }

}