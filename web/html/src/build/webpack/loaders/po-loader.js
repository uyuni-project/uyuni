/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { po } from "gettext-parser";

export default function (source) {
  this.cacheable();
  return JSON.stringify(po.parse(source, "utf8"));
}
