/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { renderer } from "./system-cve.renderer";

jest.mock("core/spa/spa-renderer", () => ({
  __esModule: true,
  default: {
    renderNavigationReact: jest.fn(),
  },
}));

jest.mock("./system-cve", () => ({
  SystemCve: () => null,
}));

const renderNavigationReact = SpaRenderer.renderNavigationReact as jest.Mock;

describe("System CVE renderer", () => {
  beforeEach(() => {
    document.body.innerHTML = '<div id="system-cve"></div>';
    renderNavigationReact.mockClear();
  });

  test("uses renderer properties", () => {
    renderer("system-cve", { serverId: 123, ovalState: "ENABLED" });

    const [element, container] = renderNavigationReact.mock.calls[0];
    expect(element.props).toMatchObject({ serverId: 123, ovalState: "ENABLED" });
    expect(container).toBe(document.getElementById("system-cve"));
  });

  test("does not render without properties", () => {
    renderer("system-cve");

    expect(renderNavigationReact).not.toHaveBeenCalled();
  });
});
