/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { render, waitFor } from "@testing-library/react";
import Tooltip from "bootstrap/js/dist/tooltip";

import { initializeTooltips } from "./tooltips";

const originalBootstrap = globalThis.bootstrap;
const instances = new Set<Tooltip>();
const instanceFor = (element: Element) => {
  const instance = Tooltip.getInstance(element);
  if (!instance) throw new Error("Tooltip not initialized");
  instances.add(instance);
  return instance;
};

beforeAll(() => Object.assign(globalThis, { bootstrap: { Tooltip } }));
afterAll(() => Object.assign(globalThis, { bootstrap: originalBootstrap }));
afterEach(async () => {
  instances.forEach((instance) => instance.dispose());
  instances.clear();
  document.body.innerHTML = "";
  await Promise.resolve();
});

test("initializes each trigger and its event listeners only once", async () => {
  const view = render(
    <button data-bs-toggle="tooltip" title="Help">
      Help
    </button>
  );
  const button = view.getByRole("button");
  initializeTooltips();
  const tooltip = instanceFor(button);
  const hide = jest.spyOn(tooltip, "hide");
  initializeTooltips();
  await Promise.resolve();
  expect(Tooltip.getInstance(button)).toBe(tooltip);
  button.click();
  expect(hide).toHaveBeenCalledTimes(1);
});

test("restores content when a reused pagination button is enabled again", async () => {
  const view = render(
    <button data-bs-toggle="tooltip" title="Next" aria-label="Next page">
      Next
    </button>
  );
  const button = view.getByRole("button");
  initializeTooltips();
  const tooltip = instanceFor(button);
  tooltip.show();
  await waitFor(() => expect(document.querySelector(".tooltip-inner")?.textContent).toBe("Next"));
  view.rerender(
    <button disabled aria-label="Next page">
      Next
    </button>
  );
  button.removeAttribute("data-bs-original-title");
  await waitFor(() => expect(document.querySelector(".tooltip")).toBeNull());
  tooltip.show();
  expect(document.querySelector(".tooltip")).toBeNull();
  await waitFor(() => expect(button.getAttribute("data-bs-original-title")).toBeNull());
  view.rerender(
    <button data-bs-toggle="tooltip" title="Next" aria-label="Next page">
      Next
    </button>
  );
  await waitFor(() => expect(button.getAttribute("data-bs-original-title")).toBe("Next"));
  expect(Tooltip.getInstance(button)).toBe(tooltip);
  tooltip.show();
  await waitFor(() => expect(document.querySelector(".tooltip-inner")?.textContent).toBe("Next"));
  expect(button.getAttribute("aria-label")).toBe("Next page");
});

test("refreshes visible tooltips and generated labels when a status icon changes", async () => {
  const view = render(<i data-bs-toggle="tooltip" title="Queued" />);
  const icon = view.container.querySelector("i")!;
  initializeTooltips();
  const tooltip = instanceFor(icon);
  tooltip.show();
  await waitFor(() => expect(document.querySelector(".tooltip-inner")?.textContent).toBe("Queued"));
  view.rerender(<i data-bs-toggle="tooltip" title="Building" />);
  await waitFor(() => expect(document.querySelector(".tooltip-inner")?.textContent).toBe("Building"));
  expect(view.container.querySelector("i")).toBe(icon);
  expect(Tooltip.getInstance(icon)).toBe(tooltip);
  expect(icon.getAttribute("aria-label")).toBe("Building");
  expect(icon.hasAttribute("title")).toBe(false);
});
