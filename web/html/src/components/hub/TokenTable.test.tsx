/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { localizedMoment } from "utils/datetime";
import { Utils } from "utils/functions";
import Network from "utils/network";
import { render, screen } from "utils/test-utils";

import { createAccessTokens } from "./story-fixtures";
import { TokenTable } from "./TokenTable";

test("renders relative expiration information for expired tokens", async () => {
  const token = createAccessTokens()[2];
  token.expirationDate = localizedMoment().subtract(1, "hour").toDate();
  const request = jest
    .spyOn(Network, "get")
    .mockReturnValue(Utils.cancelable(Promise.resolve({ items: [token], total: 1 })));

  try {
    render(<TokenTable allowToggleValidity={false} allowDeletion={false} />);
    const expired = await screen.findByText(/Yes, expired/);
    expect(expired.querySelector("time")).not.toBeNull();
    expect(expired.textContent).toMatch(/ago/);
  } finally {
    request.mockRestore();
  }
});
