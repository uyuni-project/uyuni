/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

// Type only, importing "bootstrap" itself pulls in the jQuery plugin types, which clash with our declarations
import type Tooltip from "bootstrap/js/dist/tooltip";

const TOOLTIP_SELECTOR = '[data-bs-toggle="tooltip"]';
// Bootstrap moves the native `title` here when it takes an element over, and reads the text from there afterwards
const ORIGINAL_TITLE_ATTRIBUTE = "data-bs-original-title";
const TOOLTIP_INNER_SELECTOR = ".tooltip-inner";

let isListening = false;

const triggerOf = (event: Event) => (event.target instanceof Element ? event.target.closest(TOOLTIP_SELECTOR) : null);

// Bootstrap reads the `title` attribute only when it initializes an element, but React keeps the node and just
// updates the attribute, for example when a button switches to explaining why it is disabled
const syncTitle = (el: Element, tooltip: Tooltip) => {
  const title = el.getAttribute("title");
  if (!title) {
    return;
  }

  // Leaving it in place would show the browser's own tooltip next to ours
  el.removeAttribute("title");
  if (title === el.getAttribute(ORIGINAL_TITLE_ATTRIBUTE)) {
    return;
  }

  el.setAttribute(ORIGINAL_TITLE_ATTRIBUTE, title);
  tooltip.setContent({ [TOOLTIP_INNER_SELECTOR]: title });
};

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
    const tooltip = bootstrap.Tooltip.getInstance(el);
    if (!tooltip) {
      return;
    }

    // Showing and hiding end in a callback Bootstrap queues on the tooltip's transition, and `dispose()` clears
    // the fields that callback reads. Ending the transition now runs it while the instance is still intact.
    const tipId = el.getAttribute("aria-describedby");
    const tip = tipId ? document.getElementById(tipId) : null;
    tip?.dispatchEvent(new Event("transitionend"));

    // `dispose()` restores the `title` attribute, so the element can be initialized again if it's reattached
    tooltip.dispose();
  });
};

// Keep a single instance per element, Bootstrap's own registry tells us which elements already have one. A second
// instance would leave the first one behind, still listening and still rendering tooltips nothing can hide anymore.
const initTooltips = () => {
  document.querySelectorAll(TOOLTIP_SELECTOR).forEach((el) => {
    const tooltip = bootstrap.Tooltip.getOrCreateInstance(el, { trigger: "hover" });
    syncTitle(el, tooltip);
  });
};

export function initializeTooltips() {
  initTooltips();
  if (isListening) return;

  // Delegated rather than bound to each trigger, Bootstrap's `dispose()` only removes its own listeners, so ours
  // would pile up on every element that gets initialized again
  document.addEventListener("click", (event) => {
    const trigger = triggerOf(event);
    const tooltip = trigger && bootstrap.Tooltip.getInstance(trigger);
    if (!tooltip) return;

    tooltip.hide();
    // Hover can queue the tooltip to appear after the click, so disable it to prevent that.
    // The next hover enables it again.
    tooltip.disable();
  });

  // `mouseenter` doesn't bubble, so watch `mouseover` and ignore the moves that stay within the trigger
  document.addEventListener("mouseover", (event) => {
    const trigger = triggerOf(event);
    if (!trigger || trigger.contains(event.relatedTarget as Node | null)) return;

    bootstrap.Tooltip.getInstance(trigger)?.enable();
  });

  new MutationObserver((mutations) => {
    mutations.forEach((mutation) =>
      mutation.removedNodes.forEach((node) => {
        // React moves nodes instead of recreating them, and a moved node is back in the document by the time the
        // observer runs, so only clean up after the ones that are really gone
        if (!node.isConnected) {
          disposeTooltipsIn(node);
        }
      })
    );
    initTooltips();
  }).observe(document.body, {
    childList: true,
    subtree: true,
    // A rerender can change nothing but the title of an element we already know about
    attributeFilter: ["title"],
  });
  isListening = true;
}
