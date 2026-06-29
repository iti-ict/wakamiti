import JSZip from "jszip";
import { navigate } from "astro:transitions/client";

const baseUrl = import.meta.env.BASE_URL || "/";
const LANGUAGE_SWITCH_DELAY_MS = 250;
let handlersRegistered = false;

function applyTheme(theme) {
  const root = document.documentElement;
  root.classList.remove("bright", "dark");
  root.classList.add(theme);
  localStorage.setItem("theme", theme);
}

function initializeTheme() {
  const storedTheme = localStorage.getItem("theme") || "bright";
  applyTheme(storedTheme);
}

function syncTheme() {
  const storedTheme = localStorage.getItem("theme") || "bright";
  applyTheme(storedTheme);
}

function closeSidebar() {
  document.body.classList.remove("sidebar-open");
}

function toggleSidebar() {
  document.body.classList.toggle("sidebar-open");
}

function getHashTarget(hash) {
  if (!hash) {
    return null;
  }

  const id = decodeURIComponent(hash).replace(/^#/, "");
  return id ? document.getElementById(id) : null;
}

function updateHeaderState() {
  const header = document.querySelector("[data-header]");
  if (!header) {
    return;
  }

  header.classList.toggle("header--scrolled", window.scrollY > 40);
}

function updateCurrentSidebarLink() {
  const links = Array.from(document.querySelectorAll(".topic.is-active + ul .sub-topic"));
  if (links.length === 0) {
    return;
  }

  const fromTop = window.scrollY + 120;
  let current;

  for (const link of links) {
    if (link.hash) {
      const section = getHashTarget(link.hash);
      if (section && section.offsetTop <= fromTop) {
        current = link;
      }
    } else {
      const currentUrl = window.location.href.replace(/#.*/g, "").replace(/\/$/g, "");
      if (currentUrl.endsWith(link.getAttribute("href").replace(/\/$/g, ""))) {
        current = link;
      }
    }
  }

  for (const link of links) {
    link.classList.toggle("current", link === current);
  }
}

function updateCurrentPageIndex() {
  const links = Array.from(document.querySelectorAll("[data-page-index-link]"));
  const sections = Array.from(document.querySelectorAll("[data-page-index-section]"));
  if (links.length === 0) {
    return;
  }

  const fromTop = window.scrollY + 140;
  let current = links[0];

  for (const link of links) {
    if (!link.hash) {
      continue;
    }

    const section = getHashTarget(link.hash);
    if (section && section.offsetTop <= fromTop) {
      current = link;
    }
  }

  for (const link of links) {
    link.classList.toggle("current", link === current);
  }

  const currentSectionAnchor =
    current.getAttribute("data-page-index-depth") === "2"
      ? current.getAttribute("data-page-index-anchor")
      : current.getAttribute("data-page-index-parent");

  for (const section of sections) {
    section.classList.toggle("current-section", section.getAttribute("data-page-index-section") === currentSectionAnchor);
  }
}

function setCopiedState(button) {
  const copyIcon = button.querySelector(".clipboard-copy-icon");
  const checkIcon = button.querySelector(".clipboard-check-icon");
  copyIcon?.classList.add("hidden");
  checkIcon?.classList.remove("hidden");

  window.setTimeout(() => {
    copyIcon?.classList.remove("hidden");
    checkIcon?.classList.add("hidden");
  }, 3000);
}

async function copyCode(button) {
  const code = button.parentElement?.querySelector("pre.hidden")?.textContent;
  if (!code) {
    return;
  }

  await navigator.clipboard.writeText(code);
  setCopiedState(button);
}

async function downloadTutorial() {
  const zip = new JSZip();
  const sourceBase = "https://raw.githubusercontent.com/iti-ict/wakamiti/main/examples/tutorial";
  const files = ["application-wakamiti.properties", "docker-compose.yml", "readme.md"];

  await Promise.all(
    files.map(async (file) => {
      const response = await fetch(`${sourceBase}/${file}`);
      if (!response.ok) {
        throw new Error(`Failed to download ${file}`);
      }

      zip.file(file, await response.text());
    })
  );

  const blob = await zip.generateAsync({ type: "blob" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "tutorial.zip";
  link.click();
  URL.revokeObjectURL(url);
  return false;
}

function createAsciinemaPlayers() {
  if (!window.AsciinemaPlayer) {
    return;
  }

  document.querySelectorAll(".remark-asciinema").forEach((element) => {
    if (element.querySelector(".ap-wrapper")) {
      return;
    }

    const url = element.getAttribute("data-url");
    const opts = JSON.parse(element.getAttribute("data-opts") || "{}");
    window.AsciinemaPlayer.create(`${baseUrl.replace(/\/$/, "")}/${url.replace(/^\/+/, "")}`, element, opts);
  });
}

async function ensureAsciinemaPlayer() {
  if (!document.querySelector(".remark-asciinema")) {
    return;
  }

  if (window.AsciinemaPlayer) {
    createAsciinemaPlayers();
    return;
  }

  await new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = `${baseUrl}asciinema-player.min.js`;
    script.onload = resolve;
    script.onerror = reject;
    document.head.appendChild(script);
  });

  createAsciinemaPlayers();
}

function refreshDynamicPageState() {
  updateHeaderState();
  updateCurrentSidebarLink();
  updateCurrentPageIndex();
  void ensureAsciinemaPlayer();
}

function registerHandlers() {
  if (handlersRegistered) {
    return;
  }

  handlersRegistered = true;

  document.addEventListener("click", async (event) => {
    const themeButton = event.target.closest("[data-theme-toggle]");
    if (themeButton) {
      const nextTheme = document.documentElement.classList.contains("dark") ? "bright" : "dark";
      applyTheme(nextTheme);
      return;
    }

    const menuButton = event.target.closest("[data-menu-toggle]");
    if (menuButton) {
      toggleSidebar();
      return;
    }

    const languageButton = event.target.closest("[data-language-switch]");
    if (languageButton) {
      if (languageButton.dataset.transitioning === "true") {
        return;
      }

      const target = languageButton.getAttribute("data-language-switch");
      const nextText = languageButton.getAttribute("data-language-next-text");
      if (target) {
        languageButton.dataset.transitioning = "true";
        const label = languageButton.querySelector(".language-text");
        if (label && nextText) {
          label.textContent = nextText;
        }
        window.setTimeout(() => navigate(target), LANGUAGE_SWITCH_DELAY_MS);
      }
      return;
    }

    const copyButton = event.target.closest("[data-copy-code]");
    if (copyButton) {
      await copyCode(copyButton);
      return;
    }

    if (event.target.closest(".sidebar a")) {
      closeSidebar();
    }
  });

  window.addEventListener("scroll", () => {
    updateHeaderState();
    updateCurrentSidebarLink();
    updateCurrentPageIndex();
  });

  window.addEventListener("resize", () => {
    if (window.innerWidth >= 768) {
      closeSidebar();
    }
  });

  window.addEventListener("load", () => {
    refreshDynamicPageState();
  });
}

window.downloadTutorial = downloadTutorial;
initializeTheme();
registerHandlers();
refreshDynamicPageState();

document.addEventListener("astro:after-swap", syncTheme);
document.addEventListener("astro:page-load", () => {
  refreshDynamicPageState();
});
