import { menuSettings, normalizeSlug, getLocaleFromSlug } from "./site.js";

const docModules = import.meta.glob("../../docs/**/*.md");
let cachedDocs;

function normalizeHeading(heading) {
  if (!heading) {
    return heading;
  }

  const anchor = heading.slug
    ? `#${heading.slug}`
    : heading.anchor || (heading.id ? `#${heading.id}` : undefined);

  return {
    depth: heading.depth,
    value: heading.text || heading.value,
    anchor
  };
}

export function slugToSegments(slug) {
  return normalizeSlug(slug)
    .replace(/^\/+|\/+$/g, "")
    .split("/")
    .filter(Boolean);
}

export async function getDocRecords() {
  if (cachedDocs) {
    return cachedDocs;
  }

  cachedDocs = (
    await Promise.all(
      Object.entries(docModules).map(async ([file, loader]) => {
        const mod = await loader();
        const slug = normalizeSlug(mod.frontmatter.slug);
        const headingsSource =
          typeof mod.getHeadings === "function" ? mod.getHeadings() : (mod.headings || []);

        return {
          file,
          slug,
          slugParam: slugToSegments(slug).join("/"),
          locale: getLocaleFromSlug(slug),
          title: mod.frontmatter.title,
          description: mod.frontmatter.description || "",
          headings: headingsSource
            .map(normalizeHeading)
            .filter((heading) => heading?.depth >= 2 && heading?.depth <= 3),
          frontmatter: mod.frontmatter
        };
      })
    )
  ).sort((left, right) => left.slug.localeCompare(right.slug));

  return cachedDocs;
}

export async function loadDocModule(file) {
  return docModules[file]();
}

export async function getSidebar(locale) {
  const settings = menuSettings[locale];

  return settings.sidebar.map((section) => ({
    section: section.section,
    topics: section.topics.map((topic) => {
      const normalizedSlug = normalizeSlug(topic.slug);

      return {
        title: topic.title,
        slug: normalizedSlug,
        headings: Array.isArray(topic.headings) ? topic.headings : []
      };
    })
  }));
}
