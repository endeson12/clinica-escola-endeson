/* Native dialogs: focus trap and Escape are provided by the browser. */
window.PortalUX = (() => {
  "use strict";
  const paths = {
    menu: "M3 5h14M3 10h14M3 15h14",
    close: "m5 5 10 10M15 5 5 15",
    logout: "M8 3H3v14h5M8 10h10m-4-4 4 4-4 4",
    bell: "M4 14h12l-2-3V7a4 4 0 0 0-8 0v4zM8 17h4",
    user: "M10 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6M4 17a6 6 0 0 1 12 0",
    help: "M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16M8 7a2 2 0 0 1 4 0c0 2-2 2-2 4M10 14v.2",
    calendar: "M3 5h14v13H3zM6 2v5M14 2v5M3 9h14M6 12h2M12 12h2",
    clock: "M18 10a8 8 0 1 0-16 0 8 8 0 0 0 16 0M10 5v5l3 2",
    message: "M3 3h14v11H8l-5 4zM6 7h8M6 10h5",
    award: "M14 7a4 4 0 1 0-8 0 4 4 0 0 0 8 0M7 11l-1 7 4-2 4 2-1-7",
    grid: "M3 3h6v6H3zM12 3h6v6h-6zM3 12h6v6H3zM12 12h6v6h-6z",
  };
  function icon(name) {
    return `<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="${paths[name] || paths.grid}"/></svg>`;
  }
  let pending = null;
  function confirmAction(
    message,
    {
      title = "Confirmar ação",
      accept = "Confirmar",
      cancel = "Cancelar",
    } = {},
  ) {
    const d = document.getElementById("confirm-dialog");
    if (pending) return Promise.resolve(false);
    const back = document.activeElement;
    d.querySelector("h2").textContent = title;
    d.querySelector("p").textContent = message;
    d.querySelector("[data-accept]").textContent = accept;
    d.querySelector("[data-cancel]").textContent = cancel;
    return new Promise((resolve) => {
      pending = (value) => {
        pending = null;
        d.close();
        back?.focus();
        resolve(value);
      };
      d.showModal();
      d.querySelector("[data-cancel]").focus();
    });
  }
  function init() {
    document
      .querySelectorAll("[data-ui-icon]")
      .forEach((b) => (b.innerHTML = icon(b.dataset.uiIcon)));
    const d = document.createElement("dialog");
    d.id = "confirm-dialog";
    d.setAttribute("aria-labelledby", "confirm-title");
    d.innerHTML =
      '<div class="confirm-symbol" aria-hidden="true">' +
      icon("help") +
      '</div><h2 id="confirm-title">Confirmar ação</h2><p class="dialog-help"></p><div class="form-actions"><button class="btn" data-cancel>Cancelar</button><button class="btn primary" data-accept>Confirmar</button></div>';
    document.body.append(d);
    d.querySelector("[data-cancel]").onclick = () => pending?.(false);
    d.querySelector("[data-accept]").onclick = () => pending?.(true);
    d.addEventListener("cancel", (e) => {
      e.preventDefault();
      pending?.(false);
    });
  }
  document.addEventListener("DOMContentLoaded", init);
  return { icon, confirm: confirmAction };
})();
