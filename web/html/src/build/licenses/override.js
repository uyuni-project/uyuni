/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export const applyOverrides = (name, version, license) => {
  /**
   * If the license is "X OR Y" but doesn't include parentheses, wrap it so it's correct in the aggregate list
   */
  const hasParens = /^\(.*\)$/.test(license);
  if (license.includes(" OR ") && !hasParens) {
    license = `(${license})`;
  }

  /**
   * Replace deprecated SPDX identifiers like "GPL-3.0" with the "-only" variant, e.g. pwstrength-bootstrap declares "(GPL-3.0 OR MIT)"
   */
  license = license.replace(/\b((?:A|L)?GPL-\d\.\d)(?![\w.+-])/g, "$1-only");

  /**
   * These packages delcare "BSD" without any other info in the package.json, but the license text in the repo is actually BSD-3-Clause
   *
   * Schema: [NPM package name, version number]
   */
  const underspecifiedBsd = [
    ["ace-builds", "1.3.3"],
    ["metal-ajax", "2.1.1"],
    ["metal-dom", "2.16.8"],
    ["metal-debounce", "2.0.2"],
    ["metal-promise", "2.0.1"],
    ["metal-events", "2.16.8"],
    ["metal-path-parser", "1.0.4"],
    ["metal-structs", "1.0.2"],
    ["metal-uri", "2.4.0"],
    ["metal-useragent", "3.0.1"],
  ];

  if (license === "BSD") {
    if (underspecifiedBsd.find((item) => item[0] === name && item[1] === version)) {
      return "BSD-3-Clause";
    } else {
      throw new RangeError(`Unable to identify BSD license for "${name}" version "${version}"`);
    }
  }

  return license;
};
