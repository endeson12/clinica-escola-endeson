/* Preference only: no personal data or authentication state in localStorage. */
(() => {
  "use strict";
  const root = document.documentElement,
    key = "portal-color-theme";
  const media = matchMedia("(prefers-color-scheme: dark)");
  let saved;
  try {
    saved = localStorage.getItem(key);
  } catch {
    /* Storage may be disabled. */
  }
  const icons = {
    sun: "M10 6a4 4 0 1 0 0 8 4 4 0 0 0 0-8M10 1v2M10 17v2M1 10h2M17 10h2M3.6 3.6 5 5M15 15l1.4 1.4M3.6 16.4 5 15M15 5l1.4-1.4",
    moon: "M17 12A7 7 0 0 1 8 3a7 7 0 1 0 9 9",
  };
  function update(theme) {
    root.dataset.theme = theme;
    root.style.colorScheme = theme;
    document.querySelectorAll("[data-theme-toggle]").forEach((b) => {
      const label =
        theme === "dark" ? "Ativar modo claro" : "Ativar modo escuro";
      b.setAttribute("aria-label", label);
      b.title = label;
      b.innerHTML = `<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="${icons[theme === "dark" ? "sun" : "moon"]}"/></svg>`;
    });
  }
  update(
    ["light", "dark"].includes(saved)
      ? saved
      : media.matches
        ? "dark"
        : "light",
  );
  media.addEventListener("change", (e) => {
    if (!saved) update(e.matches ? "dark" : "light");
  });
  document.addEventListener("DOMContentLoaded", () => {
    if (!document.querySelector("[data-theme-toggle]")) {
      const b = document.createElement("button");
      b.type = "button";
      b.className = "icon-btn theme-toggle";
      b.dataset.themeToggle = "";
      const target = document.querySelector(
        ".topbar-tools,.topo__dir,.signin-form-section",
      );
      target?.prepend(b);
    }
    update(root.dataset.theme);
    document.querySelectorAll("[data-theme-toggle]").forEach((b) =>
      b.addEventListener("click", () => {
        saved = root.dataset.theme === "dark" ? "light" : "dark";
        try {
          localStorage.setItem(key, saved);
        } catch {}
        update(saved);
      }),
    );
  });
})();
