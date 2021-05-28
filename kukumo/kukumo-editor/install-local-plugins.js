const path = require('path');
const fs = require('fs');
const rimraf = require('rimraf');
const unzipper = require('unzipper');

const localPluginFolder = path.join(__dirname, 'local-plugins');
const pluginFolder = path.join(__dirname, 'plugins');

const plugins = fs.readdirSync(localPluginFolder).filter(name => name.endsWith('.vsix'));
for (const plugin of plugins) {
    const pluginName = plugin.replace(/-\d+.\d+.*/, '');
    const localPluginPath = path.join(localPluginFolder, plugin);
    const targetPluginPath = path.join(pluginFolder, pluginName);
    if (fs.existsSync(targetPluginPath)) {
        rimraf.sync(targetPluginPath);
    }
    fs.createReadStream(localPluginPath)
      .pipe(unzipper.Extract({ path: targetPluginPath }));
}