import { visit } from "unist-util-visit";

function isExternal(url) {
  return /^(?:[a-z]+:)?\/\//i.test(url);
}

function withBase(base, url) {
  if (!url || url.startsWith("#")) {
    return url;
  }

  if (isExternal(url) || url.startsWith("mailto:") || url.startsWith("javascript:") || url.startsWith("asciinema:")) {
    return url;
  }

  const basePath = base === "/" ? "" : base.replace(/\/$/, "");
  const normalized = url.startsWith("/") ? url : `/${url}`;
  return `${basePath}${normalized}`;
}

export default function prefixInternalLinks(options = {}) {
  const base = options.base || "/";

  return (tree) => {
    visit(tree, "link", (node) => {
      node.url = withBase(base, node.url);
    });

    visit(tree, "image", (node) => {
      if (node.url && !node.url.startsWith("asciinema:")) {
        node.url = withBase(base, node.url);
      }
    });
  };
}
