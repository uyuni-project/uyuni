let isListening = false;
const initializedTooltips = new WeakSet<Element>();

export function initializeTooltips() {
  // Initialize tooltips on existing elements

  const initTooltips = () => {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach((el) => {
      if (initializedTooltips.has(el)) {
        return;
      }

      const tooltip = bootstrap.Tooltip.getOrCreateInstance(el, {
        trigger: "hover",
      });

      initializedTooltips.add(el);

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
