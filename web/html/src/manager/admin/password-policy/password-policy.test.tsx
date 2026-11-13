/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { render } from "utils/test-utils";

import { PasswordPolicyData } from "./password_policy_type";
import PasswordPolicy from "./password-policy";

const policy: PasswordPolicyData = {
  minLength: BigInt(4),
  maxLength: BigInt(32),
  digitFlag: false,
  lowerCharFlag: false,
  upperCharFlag: false,
  consecutiveCharsFlag: false,
  specialCharFlag: false,
  specialChars: null,
  restrictedOccurrenceFlag: false,
  maxCharacterOccurrence: BigInt(2),
};

describe("Password policy", () => {
  test("shows the Manager Configuration tabs with Password Policy active", () => {
    render(<PasswordPolicy policy={policy} defaults={policy} />);

    const active = document.querySelector(".nav-tabs li.active a");
    expect(active?.getAttribute("href")).toBe("/rhn/manager/admin/config/password-policy?");
  });
});
