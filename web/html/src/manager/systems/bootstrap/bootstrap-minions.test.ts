/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { formatActivationKeyLabel } from "./activation-key-option";

describe("formatActivationKeyLabel", () => {
  test("shows description with the key when a description is present", () => {
    expect(
      formatActivationKeyLabel({
        key: "1-23ab48de37fe",
        description: "CentOS 7",
      })
    ).toBe("CentOS 7 (1-23ab48de37fe)");
  });

  test("shows only the key when the description is missing", () => {
    expect(formatActivationKeyLabel({ key: "1-23ab48de37fe" })).toBe("1-23ab48de37fe");
  });

  test("shows only the key when the description is blank", () => {
    expect(formatActivationKeyLabel({ key: "1-23ab48de37fe", description: "  " })).toBe("1-23ab48de37fe");
  });
});
