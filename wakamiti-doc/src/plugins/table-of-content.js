


module.exports = (options) => {
    return (node) => {
        const result = require('mdast-util-toc')(node, Object.assign({},
            {heading: "Table of content|Tabla de contenido", maxDepth: 3}
        ))
        if (result.endIndex === null || result.index === null || result.index === -1 || !result.map) {
            return
        }
        result.map.data = {id: 'toc', htmlAttributes: {id: 'toc'}, hProperties: {id: 'toc'}}
        node.children = [
            ...node.children.slice(0, result.index - 1),
            result.map,
            ...node.children.slice(result.endIndex - 1)
        ]
    }
}