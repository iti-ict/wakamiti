import { toc } from "mdast-util-toc";

export default function tableOfContent() {
  return (node) => {
    const result = toc(node, {
      heading: "(Table of content|Table of contents|Tabla de contenido|Tabla de contenidos)?",
      maxDepth: 3
    });

    if (result.endIndex === null || result.index === null || result.index === -1 || !result.map) {
      return;
    }

    result.map.data = {
      id: "toc",
      htmlAttributes: { id: "toc" },
      hProperties: { id: "toc" }
    };

    node.children = [
      ...node.children.slice(0, result.index - 1),
      result.map,
      ...node.children.slice(result.endIndex - 1)
    ];
  };
}
