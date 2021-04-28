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