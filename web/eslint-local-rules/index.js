/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import intlApostropheCurly from "./intl-apostrophe-curly.js";
import noRawDate from "./no-raw-date.js";

const plugin = {
  rules: {
    "no-raw-date": noRawDate,
    "intl-apostrophe-curly": intlApostropheCurly,
  },
};
export default plugin;
