/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { click, render, screen, waitFor, within } from "utils/test-utils";

import { PackageListActionScheduler } from "./PackageListActionScheduler";
import { installPackageMocks } from "./story-fixtures";

const originalHandleSst = globalThis.handleSst;
beforeAll(() => Object.assign(globalThis, { handleSst: jest.fn() }));
afterAll(() => Object.assign(globalThis, { handleSst: originalHandleSst }));

const props = {
  serverId: 1,
  selectionSet: "storybook_ptf_install",
  actionChains: [],
  icon: "fa-package",
  listDataAPI: "/storybook/packages",
  scheduleActionAPI: "/storybook/packages/scheduleAction",
  actionType: "packages.update",
  listTitle: "Packages",
  listSummary: "Select packages",
  listEmptyText: "No packages",
  listActionLabel: "Install PTFs",
  listColumns: [],
  confirmTitle: "Confirm packages",
};

let restore: () => void;
beforeEach(() => {
  restore = installPackageMocks();
});
afterEach(() => restore());

const checkboxFor = (name: string) => within(screen.getByRole("row", { name })).getByRole("checkbox");

test("preserves selection after confirmation and clears it after scheduling", async () => {
  render(<PackageListActionScheduler {...props} />);
  await screen.findByText("ptf-18134-2-1");
  await click(checkboxFor("ptf-18134-2-1"));
  await click(screen.getByRole("button", { name: "Install PTFs" }));
  await click(await screen.findByRole("button", { name: "Back to list" }));

  await screen.findByText("ptf-18134-2-1");
  expect((checkboxFor("ptf-18102-1-1") as HTMLInputElement).checked).toBe(true);
  expect((checkboxFor("ptf-18134-2-1") as HTMLInputElement).checked).toBe(true);

  await click(screen.getByRole("button", { name: "Install PTFs" }));
  await click(await screen.findByRole("button", { name: "Confirm" }));
  await screen.findByText("ptf-18134-2-1");
  await waitFor(() => expect((checkboxFor("ptf-18102-1-1") as HTMLInputElement).checked).toBe(false));
  expect((checkboxFor("ptf-18134-2-1") as HTMLInputElement).checked).toBe(false);
});

test("starts a fresh visit with the initial selection", async () => {
  const view = render(<PackageListActionScheduler {...props} />);
  await screen.findByText("ptf-18134-2-1");
  await click(checkboxFor("ptf-18134-2-1"));
  view.unmount();
  restore();
  restore = installPackageMocks();
  render(<PackageListActionScheduler {...props} />);

  await screen.findByText("ptf-18134-2-1");
  expect((checkboxFor("ptf-18102-1-1") as HTMLInputElement).checked).toBe(true);
  expect((checkboxFor("ptf-18134-2-1") as HTMLInputElement).checked).toBe(false);
});

test("selects all available packages across pages without selecting unavailable packages", async () => {
  render(<PackageListActionScheduler {...props} />);
  await screen.findByText("ptf-18134-2-1");
  await click(screen.getByRole("button", { name: /Select all .* across all pages/ }));

  await waitFor(() => expect((checkboxFor("ptf-18134-2-1") as HTMLInputElement).checked).toBe(true));
  expect((checkboxFor("ptf-18102-1-1") as HTMLInputElement).checked).toBe(true);
  expect((checkboxFor("ptf-18201-1-2") as HTMLInputElement).checked).toBe(false);
  expect((checkboxFor("ptf-18201-1-2") as HTMLInputElement).disabled).toBe(true);

  await click(screen.getByRole("button", { name: "Install PTFs" }));
  await click(await screen.findByRole("button", { name: "Back to list" }));
  await screen.findByText("ptf-18134-2-1");
  expect((checkboxFor("ptf-18134-2-1") as HTMLInputElement).checked).toBe(true);
});
