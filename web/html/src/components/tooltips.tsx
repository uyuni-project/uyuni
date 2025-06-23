import { Tooltip } from "bootstrap";

export function initializeTooltips() {
  // Initialize tooltips on existing elements

  const initTooltips = () => {
    const elements = document.querySelectorAll('[data-bs-toggle="tooltip"]:not([data-tooltip-initialized])');
    elements.forEach((el) => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const tooltip = new Tooltip(el, {
        trigger: el.getAttribute("data-bs-trigger") || "hover",
      });
      el.setAttribute("data-tooltip-initialized", "true");
    });
  };

  initTooltips();

  new MutationObserver(() => initTooltips()).observe(document.body, {
    childList: true,
    subtree: true,
  });
}

export function disposeTooltips() {
  const elements = document.querySelectorAll('[data-bs-toggle="tooltip"]');
  elements.forEach((el) => {
    const tooltip = Tooltip.getInstance(el);
    if (tooltip) {
      tooltip.dispose();
      el.removeAttribute("data-tooltip-initialized");
    }
  });
}
