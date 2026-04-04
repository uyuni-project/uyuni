let isListening = false;
export function initializeTooltips() {
  // Initialize tooltips on existing elements

  const initTooltips = () => {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach((el) => {
      const tooltip = new bootstrap.Tooltip(el, {
        trigger: "hover",
      });

      el.addEventListener("click", () => tooltip.hide());
      el.addEventListener("mouseleave", () => tooltip.hide());
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
