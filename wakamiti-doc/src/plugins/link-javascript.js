
const visit = require('unist-util-visit')

module.exports = (options) => {

    return (tree) => {
        visit(tree, "link", (node, index, parent) => {
            if (node.url == null || !node.url.startsWith('javascript:')) return;
            delete node.data.hProperties.target
            parent.children[index] = node
        });
    }
}