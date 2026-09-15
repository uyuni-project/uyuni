/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { replacer } from "./json";

test("check Map stringify", () => {
  expect(JSON.stringify(new Map([["a", 1]]), replacer)).toEqual('{"a":1}');
});
