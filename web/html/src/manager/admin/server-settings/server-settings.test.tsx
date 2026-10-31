/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { Utils } from "utils/functions";
import Network from "utils/network";
import { click, render, screen, type } from "utils/test-utils";

import ServerSettings, { changedValues } from "./server-settings";
import { ServerSetting } from "./server-settings.types";

const settings: ServerSetting[] = [
  {
    key: "REPOSYNC_TIMEOUT",
    description: "Timeout of reposync downloads in seconds",
    type: "INTEGER",
    value: "300",
    defaultValue: "300",
    modified: false,
  },
  {
    key: "SYNC_SOURCE_PACKAGES",
    description: "Synchronize source packages with reposync",
    type: "BOOLEAN",
    value: "false",
    defaultValue: "false",
    modified: false,
  },
  {
    key: "NO_PROXY",
    description: "Comma separated list of hosts",
    type: "LIST",
    value: "example.com",
    defaultValue: null,
    modified: true,
  },
];

describe("Server settings", () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  test("changedValues returns only the changed values", () => {
    expect(changedValues(settings, { REPOSYNC_TIMEOUT: "300", SYNC_SOURCE_PACKAGES: false, NO_PROXY: "" })).toEqual({
      NO_PROXY: "",
    });
    expect(changedValues(settings, { REPOSYNC_TIMEOUT: "600", SYNC_SOURCE_PACKAGES: true })).toEqual({
      REPOSYNC_TIMEOUT: "600",
      SYNC_SOURCE_PACKAGES: "true",
    });
  });

  test("renders all settings with description and default", () => {
    render(<ServerSettings settings={settings} />);

    expect(screen.getByLabelText<HTMLInputElement>("REPOSYNC_TIMEOUT").value).toBe("300");
    expect(screen.getByLabelText<HTMLInputElement>("SYNC_SOURCE_PACKAGES").checked).toBe(false);
    expect(screen.getByLabelText<HTMLInputElement>("NO_PROXY").value).toBe("example.com");
    expect(screen.getByText("Timeout of reposync downloads in seconds", { exact: false })).not.toBeNull();
    expect(screen.getByText("Default: not set", { exact: false })).not.toBeNull();
  });

  test("offers reset only for modified settings", () => {
    render(<ServerSettings settings={settings} />);

    expect(document.getElementById("reset-NO_PROXY")).not.toBeNull();
    expect(document.getElementById("reset-REPOSYNC_TIMEOUT")).toBeNull();
  });

  test("saves only the changed values", async () => {
    const post = jest
      .spyOn(Network, "post")
      .mockReturnValue(Utils.cancelable(Promise.resolve({ success: true, messages: [], data: settings })));
    render(<ServerSettings settings={settings} />);

    await type(screen.getByLabelText("REPOSYNC_TIMEOUT"), "600");
    await click(screen.getByLabelText("SYNC_SOURCE_PACKAGES"));
    await click(screen.getByText("Save"));

    expect(post).toHaveBeenCalledWith("/rhn/manager/api/admin/config/server-settings", {
      REPOSYNC_TIMEOUT: "600",
      SYNC_SOURCE_PACKAGES: "true",
    });
  });

  test("resets a setting", async () => {
    const post = jest
      .spyOn(Network, "post")
      .mockReturnValue(Utils.cancelable(Promise.resolve({ success: true, messages: [], data: settings })));
    render(<ServerSettings settings={settings} />);

    await click(screen.getByText("Reset to default"));

    expect(post).toHaveBeenCalledWith("/rhn/manager/api/admin/config/server-settings/reset", { key: "NO_PROXY" });
  });
});
