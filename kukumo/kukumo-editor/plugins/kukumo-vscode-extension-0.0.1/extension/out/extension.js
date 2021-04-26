"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.deactivate = exports.activate = void 0;
const language_client_1 = require("./language-client");
function activate(context) {
    console.log('Activating Kukumo VSCode extension...');
    language_client_1.start(context);
    console.log('Kukumo VSCode extension activated.');
}
exports.activate = activate;
function deactivate() {
    console.log('Kukumo VSCode extension deactivated.');
}
exports.deactivate = deactivate;
//# sourceMappingURL=extension.js.map