/**
 * These packages delcare "BSD" without a version in the package.json, but the license text in the repo is actually BSD-3-Clause
 */
const applyOverrides = (name, license) => {
  const affected = [
    "ace-builds",
    "metal-ajax",
    "metal-dom",
    "metal-debounce",
    "metal-promise",
    "metal-events",
    "metal-path-parser",
    "metal-structs",
    "metal-uri",
    "metal-useragent",
  ];
  if (license === "BSD" && affected.includes(name)) {
    return "BSD-3-Clause";
  }
  return license;
};

module.exports = {
  applyOverrides,
};
