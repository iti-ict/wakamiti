import { visit } from "unist-util-visit";

export default function linkJavascript() {
  return (tree) => {
    visit(tree, "link", (node) => {
      if (node.url == null || !node.url.startsWith("javascript:")) {
        return;
      }

      if (node.data?.hProperties) {
        delete node.data.hProperties.target;
      }
    });
  };
}
