
const querystring = require('querystring')
const visit = require('unist-util-visit')

module.exports = (options) => {

    return (tree) => {
        visit(tree, "image", (node, index, parent) => {
            if (node.url == null || !node.url.startsWith('asciinema:')) return;
            const parts = node.url.replace('asciinema:', '').split('?')
            const opts= querystring.parse(parts.length > 1 ? parts[1] : "", " ");

            const toString = obj => Object.entries(obj).map(([k, v]) => `${k}: '${v}'`).join(', ');

            parent.children[index] = {
                // type: "wrapper",
                // data: {
                //     hName: "div",
                //     hProperties: {
                //         className: "remark-asciinema",
                //     },
                // },
                // children: [
                //     {
                        type: "html",
                        value: `<div class="remark-asciinema" id="${node.alt}" data-url="${parts[0]}" data-opts="{${toString(opts)}}"></div>`,
                    // },
//                     {
//                         type: "html",
//                         value: `
// <script async>AsciinemaPlayer.create('${options.pathPrefix}/${parts[0]}', document.getElementById('${node.alt}'), {${toString(opts)}});</script>
// `
//                     }
//                 ],
            };
        });
    }
}