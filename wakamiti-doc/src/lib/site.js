import settingsEn from "../../data/settings_en.json";
import settingsEs from "../../data/settings_es.json";
import enMessages from "../assets/locales/en.json";
import esMessages from "../assets/locales/es.json";

export const defaultLocale = "es";
export const menuSettings = {
  es: settingsEs,
  en: settingsEn
};
export const messages = {
  es: esMessages,
  en: enMessages
};

export function normalizeSlug(slug = "/") {
  const trimmed = slug.replace(/^\/+|\/+$/g, "");
  return trimmed ? `/${trimmed}` : "/";
}

export function getLocaleFromSlug(slug = "/") {
  const normalized = normalizeSlug(slug);
  return normalized === "/en" || normalized.startsWith("/en/") ? "en" : "es";
}

export function localizePath(path = "/", locale = defaultLocale) {
  const normalized = normalizeSlug(path);
  const bare = getLocaleFromSlug(normalized) === "en" ? normalized.replace(/^\/en(?=\/|$)/, "") || "/" : normalized;

  if (locale === "en") {
    return bare === "/" ? "/en/" : `/en${bare}`;
  }

  return bare;
}

export function withBase(path = "/") {
  const base = import.meta.env.BASE_URL || "/";
  const normalized = path.startsWith("/") ? path : `/${path}`;
  return base === "/" ? normalized : `${base.replace(/\/$/, "")}${normalized}`;
}
