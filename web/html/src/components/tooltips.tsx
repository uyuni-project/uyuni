const TOOLTIP_SELECTOR = '[data-bs-toggle="tooltip"]';
const INITIALIZED_ATTRIBUTE = "data-tooltip-initialized";

let isListening = false;

// Bootstrap adds the tooltip to `document.body`, so it can remain after its trigger is removed.
// Dispose of it when React removes the trigger, such as after a click or SPA navigation.
const disposeTooltipsIn = (node: Node) => {
  if (!(node instanceof Element)) {
    return;
  }

  const triggers = Array.from(node.querySelectorAll(TOOLTIP_SELECTOR));
  if (node.matches(TOOLTIP_SELECTOR)) {
    triggers.push(node);
  }

  triggers.forEach((el) => {
    bootstrap.Tooltip.getInstance(el)?.dispose();
    // `dispose()` restores the `title` attribute, so the element can be initialized again if it's reattached
    el.removeAttribute(INITIALIZED_ATTRIBUTE);
  });
};

export function initializeTooltips() {
  // Initialize tooltips on existing elements

  const initTooltips = () => {
    // Skip the elements we already know about, the observer below runs on every DOM change and a second instance
    // would leave the first one behind, still listening and still rendering tooltips nothing can hide anymore
    const tooltipTriggerList = document.querySelectorAll(`${TOOLTIP_SELECTOR}:not([${INITIALIZED_ATTRIBUTE}])`);
    tooltipTriggerList.forEach((el) => {
      el.setAttribute(INITIALIZED_ATTRIBUTE, "true");

      const tooltip = new bootstrap.Tooltip(el, {
        trigger: "hover",
      });

      el.addEventListener("click", () => {
        tooltip.hide();
        // Hover can queue the tooltip to appear after the click, so disable it to prevent that.
        // The next hover enables it again.
        tooltip.disable();
      });
      el.addEventListener("mouseenter", () => tooltip.enable());
    });
  };

  initTooltips();
  if (isListening) return;

  new MutationObserver((mutations) => {
    mutations.forEach((mutation) => mutation.removedNodes.forEach(disposeTooltipsIn));
    initTooltips();
  }).observe(document.body, {
    childList: true,
    subtree: true,
  });
  isListening = true;
}
