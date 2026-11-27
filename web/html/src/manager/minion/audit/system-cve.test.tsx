/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { render, screen } from "@testing-library/react";

import { ALL_STATUSES, getMitreCveUrl, getSuseCveUrl, getSystemCveDataUrl, SystemCve } from "./system-cve";

jest.mock("components/table/Table", () => ({
  Table: () => null,
}));

describe("System CVE page", () => {
  test("shows only an info message when OVAL is disabled", () => {
    const view = render(<SystemCve serverId={1} ovalState="DISABLED" />);

    expect(view.getByText(/OVAL based CVE auditing is disabled/)).toBeTruthy();
    expect(view.queryByRole("table")).toBeNull();
  });

  test("shows only an info message when the system is not covered by OVAL", () => {
    const view = render(<SystemCve serverId={1} ovalState="NOT_COVERED" />);

    expect(view.getByText(/No OVAL data is available/)).toBeTruthy();
    expect(view.queryByRole("table")).toBeNull();
  });

  test("builds the data URL with the statuses as query parameter, in a stable order", () => {
    expect(getSystemCveDataUrl(42, ["PATCHED", "AFFECTED_PATCH_UNAVAILABLE"])).toBe(
      "/rhn/manager/api/systems/42/details/audit/cve?statuses=AFFECTED_PATCH_UNAVAILABLE,PATCHED"
    );
  });

  test("an empty status selection requests all statuses", () => {
    expect(getSystemCveDataUrl(42, [])).toBe(
      "/rhn/manager/api/systems/42/details/audit/cve?statuses=" + ALL_STATUSES.join(",")
    );
  });

  test("adds the selected severities as query parameter, in a stable order", () => {
    expect(getSystemCveDataUrl(42, ["PATCHED"], ["UNKNOWN", "CRITICAL"])).toBe(
      "/rhn/manager/api/systems/42/details/audit/cve?statuses=PATCHED&severities=CRITICAL,UNKNOWN"
    );
  });

  test("builds the external CVE details links like the CVE Audit page", () => {
    expect(getMitreCveUrl("CVE-2024-38822")).toBe("https://cve.mitre.org/cgi-bin/cvename.cgi?name=2024-38822");
    expect(getSuseCveUrl("CVE-2024-38822")).toBe("https://www.suse.com/security/cve/CVE-2024-38822.html");
  });

  test("shows the status and severity filters as multi-select dropdowns", () => {
    render(<SystemCve serverId={1} ovalState="ENABLED" />);

    expect(screen.getByLabelText("Status")).toBeTruthy();
    expect(screen.getByText("All statuses")).toBeTruthy();
    expect(screen.getByLabelText("Severity")).toBeTruthy();
    expect(screen.queryByRole("checkbox")).toBeNull();
  });

  test("preselects only the critical and important severities", () => {
    render(<SystemCve serverId={1} ovalState="ENABLED" />);

    expect(screen.getByText("Critical")).toBeTruthy();
    expect(screen.getByText("Important")).toBeTruthy();
    expect(screen.queryByText("Moderate")).toBeNull();
    expect(screen.queryByText("All severities")).toBeNull();
  });
});
