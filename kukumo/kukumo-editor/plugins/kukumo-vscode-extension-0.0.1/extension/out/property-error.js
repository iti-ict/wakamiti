"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.PropertyError = void 0;
const vscode = require("vscode");
class PropertyError {
    constructor(property, message) {
        this.property = property;
        this.message = message;
    }
    showError() {
        vscode.window.showErrorMessage(`Value of property ${this.property} is not valid: ${this.message}`, 'Change property value').then(action => this.openPreferences());
    }
    openPreferences() {
        vscode.commands.executeCommand('workbench.action.openSettings', this.property);
    }
}
exports.PropertyError = PropertyError;
//# sourceMappingURL=property-error.js.map