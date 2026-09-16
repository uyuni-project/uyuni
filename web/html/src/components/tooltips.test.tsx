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

const renderButton = () => {
  document.body.innerHTML = `<button id="btn" data-bs-toggle="tooltip" title="Tip">Click me</button>`;
  initializeTooltips();
  return document.getElementById("btn")!;
};

afterEach(async () => {
  document.body.innerHTML = "";
  // Let the observer clean up the tooltips of the removed elements
  await wait(10);
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
