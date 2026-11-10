/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

let isListening = false;
const initializedTooltips = new WeakMap<
  Element,
  {
    tooltip: InstanceType<typeof bootstrap.Tooltip>;
    title: string;
    autoLabel: boolean;
  }
>();

const updateTooltip = (el: Element) => {
  if (!el.isConnected) return;
  const enabled = el.getAttribute("data-bs-toggle") === "tooltip" && !el.matches(":disabled");
  let state = initializedTooltips.get(el);
  if (!state) {
    if (!enabled) return;
    const autoLabel = !el.hasAttribute("aria-label") && !el.textContent?.trim();
    const tooltip = new bootstrap.Tooltip(el, { trigger: "hover" });
    state = { tooltip, title: el.getAttribute("data-bs-original-title") ?? "", autoLabel };
    initializedTooltips.set(el, state);
    el.addEventListener("click", () => tooltip.hide());
    el.addEventListener("mouseleave", () => tooltip.hide());
  }

  // Bootstrap moves the initial title into its metadata; React can set a new title on the same element.
  const nativeTitle = el.getAttribute("title");
  if (nativeTitle !== null) {
    if (el.getAttribute("data-bs-original-title") !== nativeTitle) {
      el.setAttribute("data-bs-original-title", nativeTitle);
    }
    el.removeAttribute("title");
  }
  const title = el.getAttribute("data-bs-original-title") ?? "";
  if (enabled) state.tooltip.enable();
  else {
    state.tooltip.disable();
    state.tooltip.hide();
  }
  if (state.title !== title) {
    if (state.autoLabel && (el.getAttribute("aria-label") ?? "") === state.title) {
      if (title) el.setAttribute("aria-label", title);
      else el.removeAttribute("aria-label");
    } else state.autoLabel = false;
    state.title = title;
    state.tooltip.setContent({ ".tooltip-inner": title });
  }
};

export function initializeTooltips() {
  const initTooltips = () => document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(updateTooltip);
  initTooltips();
  if (isListening) return;

  new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      if (mutation.type === "attributes") updateTooltip(mutation.target as Element);
    }
    if (mutations.some((mutation) => mutation.type === "childList")) initTooltips();
  }).observe(document.body, {
    childList: true,
    subtree: true,
    attributes: true,
    attributeFilter: ["title", "data-bs-original-title", "data-bs-toggle", "disabled"],
  });
  isListening = true;
}
