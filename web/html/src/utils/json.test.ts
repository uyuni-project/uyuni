/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { replacer } from "./json";

test("check Map stringify", () => {
  expect(JSON.stringify(new Map([["a", 1]]), replacer)).toEqual('{"a":1}');
});
