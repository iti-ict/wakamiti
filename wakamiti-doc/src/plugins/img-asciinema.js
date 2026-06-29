import querystring from "node:querystring";
import { visit } from "unist-util-visit";

export default function imgAsciinema() {
  return (tree) => {
    visit(tree, "image", (node, index, parent) => {
      if (node.url == null || !node.url.startsWith("asciinema:")) {
        return;
      }

      const parts = node.url.replace("asciinema:", "").split("?");
      const opts = querystring.parse(parts.length > 1 ? parts[1] : "", "&");

      parent.children[index] = {
        type: "wrapper",
        data: {
          hName: "div"
        },
        children: [
          {
            type: "html",
            value: `<div class="remark-asciinema" id="${node.alt}" data-url="${parts[0]}" data-opts='${JSON.stringify(opts)}'></div>`
          }
        ]
      };
    });
  };
}
