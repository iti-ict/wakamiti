import { defineConfig } from "astro/config";
import { unified } from "@astrojs/markdown-remark";
import sitemap from "@astrojs/sitemap";
import { fileURLToPath } from "node:url";

import codeButton from "./src/plugins/code-button.js";
import codeTabs from "./src/plugins/code-tabs.js";
import imgAsciinema from "./src/plugins/img-asciinema.js";
import linkJavascript from "./src/plugins/link-javascript.js";
import prefixInternalLinks from "./src/plugins/prefix-internal-links.js";
import tableOfContent from "./src/plugins/table-of-content.js";

const absoluteUrl = process.env.CI_PAGES_URL || "http://localhost:4321/";
const site = new URL(absoluteUrl);
const base = site.pathname === "/" ? "/" : site.pathname.replace(/\/$/, "");

export default defineConfig({
  site: `${site.protocol}//${site.host}`,
  base,
  integrations: [sitemap()],
  markdown: {
    syntaxHighlight: "prism",
    processor: unified({
      remarkPlugins: [
        tableOfContent,
        codeTabs,
        codeButton,
        linkJavascript,
        imgAsciinema,
        [prefixInternalLinks, { base }]
      ]
    })
  },
  vite: {
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: '@use "/src/assets/scss/config/index" as *;'
        }
      }
    },
    resolve: {
      alias: {
        "~": fileURLToPath(new URL("./src", import.meta.url))
      }
    }
  }
});
