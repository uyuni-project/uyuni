export const applyOverrides = (name, version, license) => {
  /**
   * If the license is "X OR Y" but doesn't include parentheses, wrap it so it's correct in the aggregate list
   */
  const hasParens = /^\(.*\)$/.test(license);
  if (license.includes(" OR ") && !hasParens) {
    license = `(${license})`;
  }

  /**
   * pwstrength-bootstrap uses the deprecated SPDX identifier GPL-3.0, which
   * SPDX defines as GPL-3.0-only. Keep generated license metadata explicit.
   */
  if (name === "pwstrength-bootstrap" && version === "1.2.6" && license === "(GPL-3.0 OR MIT)") {
    return "(GPL-3.0-only OR MIT)";
  }

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
