import * as vscode from 'vscode';
import { start as startLanguageClient } from './language-client';



export function activate(context: vscode.ExtensionContext) {
	console.log('Activating Kukumo VSCode extension...');
	startLanguageClient(context);	
	console.log('Kukumo VSCode extension activated.');
}




export function deactivate() {
	console.log('Kukumo VSCode extension deactivated.');
}



