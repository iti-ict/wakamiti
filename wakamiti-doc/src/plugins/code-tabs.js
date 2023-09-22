
const querystring = require('querystring')
const visit = require('unist-util-visit')


module.exports = (options) => {

    function createTabs(tabs) {
        return {
            type: "wrapper",
            data: {
                hName: "div",
                hProperties: {
                    id: tabs,
                    className: "remark-code-tabs",
                },
            },
            children: [],
        };
    }

    function createTab(node, meta, index) {
        const tabGroup = meta.tabs;
        const tabId = `${tabGroup}#${index}`;
        const tabName = meta.name;

        return [
            {
                type: "html",
                value: `<label for="${tabId}" class="remark-code-tab">
                        <input type="radio" id=${tabId} name="${tabGroup}" ${index === 0 ? "checked" : ""}/> ${tabName}
                    </label>`,
            },
            {
                type: "wrapper",
                data: {
                    hName: "div",
                    hProperties: {
                        className: "remark-code-content",
                    },
                },
                children: [node],
            },
        ];
    }

    function createPlaceholder({ tabs, name }) {
        return {
            type: "html",
            value: `<hr class="remark-code-tabs-x" rel="${tabs}#${name}" />`,
        };
    }

    function transform(tree) {
        const queue = {};

        visit(tree, "code", function (node, index, parent) {
            const meta = querystring.parse(node.meta == null ? "" : node.meta, " ");
            if (!meta.tabs) return;

            const newNode = !queue[meta.tabs]
                ? createTabs(meta.tabs)
                : createPlaceholder(meta);

            parent.children[index] = newNode;
            if (queue[meta.tabs] == null) queue[meta.tabs] = newNode;

            queue[meta.tabs].children.push(
                ...createTab(node, meta, queue[meta.tabs].children.length)
            );
        });
    }

    return transform;
};