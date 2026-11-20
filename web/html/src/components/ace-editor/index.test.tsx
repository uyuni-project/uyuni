/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { render, waitFor } from "@testing-library/react";
import type { Ace } from "ace-builds";

import { AceEditor } from "./index";

jest.unmock("components/ace-editor");

jest.mock("ace-builds/src-noconflict/theme-monokai", () => ({}));

test("updates the Ace theme and destroys the editor on unmount", async () => {
  const originalAce = window.ace;
  const originalClass = document.body.className;
  const setTheme = jest.fn();
  const destroy = jest.fn();
  const editor = {
    setTheme,
    destroy,
    setShowPrintMargin: jest.fn(),
    on: jest.fn(),
    getSession: () => ({ setValue: jest.fn(), getValue: () => "" }),
    setOptions: jest.fn(),
    setReadOnly: jest.fn(),
  } as unknown as Ace.Editor;
  window.ace = { edit: jest.fn(() => editor) };
  document.body.className = "theme-uyuni";
  try {
    const view = render(<AceEditor />);
    expect(setTheme).toHaveBeenLastCalledWith("ace/theme/xcode");
    document.body.className = "theme-suse-dark";
    await waitFor(() => expect(setTheme).toHaveBeenLastCalledWith("ace/theme/monokai"));
    document.body.className = "theme-suse-light";
    await waitFor(() => expect(setTheme).toHaveBeenLastCalledWith("ace/theme/xcode"));
    view.unmount();
    expect(destroy).toHaveBeenCalledTimes(1);
  } finally {
    window.ace = originalAce;
    document.body.className = originalClass;
  }
});
