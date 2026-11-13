/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

// Importing "bootstrap" itself pulls in the jQuery plugin types, which clash with our own declarations
import Tooltip from "bootstrap/js/dist/tooltip";

import { initializeTooltips } from "components/tooltips";

// Bootstrap is a global provided by the JSP pages, see web/html/src/global.d.ts
(global as any).bootstrap = { Tooltip };

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// Bootstrap listens to `mouseover`/`mouseout` internally, see the `customEvents` map in its EventHandler
const hover = (el: Element) => {
  el.dispatchEvent(new MouseEvent("mouseover", { bubbles: true }));
  el.dispatchEvent(new MouseEvent("mouseenter"));
};

const visibleTooltips = () => document.querySelectorAll(".tooltip").length;

const tooltipText = () => document.querySelector(".tooltip-inner")?.textContent;

const renderButton = () => {
  document.body.innerHTML = `<button id="btn" data-bs-toggle="tooltip" title="Tip">Click me</button>`;
  initializeTooltips();
  return document.getElementById("btn")!;
};

// Bootstrap finishes hiding in a callback queued on a transition, so a crash there is reported by jsdom instead of
// being thrown into the test
const uncaughtErrors: ErrorEvent[] = [];
const collectError = (event: ErrorEvent) => uncaughtErrors.push(event);

beforeEach(() => {
  uncaughtErrors.length = 0;
  window.addEventListener("error", collectError);
});

afterEach(async () => {
  document.body.innerHTML = "";
  // Let the observer clean up the tooltips of the removed elements
  await wait(10);
  window.removeEventListener("error", collectError);
  jest.restoreAllMocks();
  expect(uncaughtErrors.map((event) => event.message)).toEqual([]);
});

test("shows a tooltip on hover", async () => {
  const button = renderButton();

  hover(button);
  await wait(20);

  expect(visibleTooltips()).toBe(1);
});

test("hides the tooltip when the trigger is clicked", async () => {
  const button = renderButton();

  hover(button);
  await wait(20);
  button.click();
  await wait(20);

  expect(visibleTooltips()).toBe(0);
});

test("doesn't show a tooltip queued by the hover right before the click", async () => {
  const button = renderButton();

  // The browser can dispatch the click before the timeout queued by `mouseover` runs
  hover(button);
  button.click();
  await wait(20);

  expect(visibleTooltips()).toBe(0);
});

test("shows the tooltip again on the next hover after a click", async () => {
  const button = renderButton();

  hover(button);
  await wait(20);
  button.click();
  await wait(20);

  button.dispatchEvent(new MouseEvent("mouseout", { bubbles: true }));
  await wait(20);
  hover(button);
  await wait(20);

  expect(visibleTooltips()).toBe(1);
});

test("removes the tooltip when its trigger is removed from the page", async () => {
  const button = renderButton();

  hover(button);
  await wait(20);
  // The trigger is unmounted, e.g. by a re-render or an SPA navigation, so no `mouseout` ever follows
  button.remove();
  await wait(20);

  expect(visibleTooltips()).toBe(0);
});

test("doesn't fail when its trigger is removed right after a click", async () => {
  const button = renderButton();

  hover(button);
  await wait(20);
  // The click starts Bootstrap's hide transition, and the rerender it triggers removes the button before the
  // transition is over
  button.click();
  button.remove();
  await wait(20);

  expect(visibleTooltips()).toBe(0);
});

test("shows the new title when the trigger is rerendered with a different one", async () => {
  const button = renderButton();

  // React keeps the node and only updates the attribute, see e.g. components/formulas/EditGroup.tsx
  button.setAttribute("title", "Max number of items reached");
  await wait(20);
  hover(button);
  await wait(20);

  expect(tooltipText()).toBe("Max number of items reached");
});

test("shows the tooltip again after its trigger was disabled and enabled", async () => {
  const button = renderButton();

  // components/pagination.tsx drops the tooltip attributes while the button is disabled
  button.removeAttribute("data-bs-toggle");
  button.removeAttribute("data-bs-original-title");
  button.setAttribute("disabled", "");
  await wait(20);

  button.removeAttribute("disabled");
  button.setAttribute("data-bs-toggle", "tooltip");
  button.setAttribute("title", "Tip");
  await wait(20);

  hover(button);
  await wait(20);

  expect(visibleTooltips()).toBe(1);
  expect(tooltipText()).toBe("Tip");
});

test("handles a click only once after its trigger was moved around", async () => {
  const button = renderButton();
  const instance = Tooltip.getInstance(button);
  const disable = jest.spyOn(Tooltip.prototype, "disable");

  // Keyed rows are moved rather than recreated, see e.g. components/table/Table.tsx
  const container = document.createElement("div");
  document.body.appendChild(container);
  for (let i = 0; i < 3; i++) {
    container.appendChild(button);
    await wait(5);
    document.body.appendChild(button);
    await wait(5);
  }

  expect(Tooltip.getInstance(button)).toBe(instance);

  button.click();
  expect(disable).toHaveBeenCalledTimes(1);
});

test("keeps a single tooltip instance per element across DOM changes", async () => {
  const button = renderButton();
  const instance = Tooltip.getInstance(button);

  // Unrelated renders keep triggering the mutation observer
  for (let i = 0; i < 3; i++) {
    document.body.appendChild(document.createElement("div"));
    await wait(5);
  }

  expect(Tooltip.getInstance(button)).toBe(instance);

  hover(button);
  await wait(20);
  expect(visibleTooltips()).toBe(1);
});
