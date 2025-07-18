import { Tooltip } from "bootstrap";

let isListening = false;
export function initializeTooltips() {
  // Initialize tooltips on existing elements

  const initTooltips = () => {
    const elements = document.querySelectorAll('[data-bs-toggle="tooltip"]:not([data-tooltip-initialized])');

    elements.forEach((el) => {
      const tooltip = Tooltip.getOrCreateInstance(el, {
        trigger: "hover",
      });

      el.setAttribute("data-tooltip-initialized", "true");
      el.addEventListener("click", () => tooltip.hide());
    });
  };

  initTooltips();
  if (isListening) return;

  new MutationObserver(() => initTooltips()).observe(document.body, {
    childList: true,
    subtree: true,
  });
  isListening = true;
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
