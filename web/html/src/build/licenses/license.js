const fs = require("fs").promises;
const path = require("path");
const { applyOverrides } = require("./override");

const readLicenseText = async (packageDirectory) => {
  const files = await fs.readdir(packageDirectory);
  const licenseFilename = files.find((name) => /^(license|licence|copying)/i.test(name));
  if (!licenseFilename) return "";
  return await fs.readFile(path.join(packageDirectory, licenseFilename), "utf8");
};

const getLicense = async (dependency, packageDirectory) => {
  const name = dependency.name;
  const version = dependency.version;

  let license = "";

  if (typeof dependency.license === "string") {
    license = applyOverrides(name, version, dependency.license);
  } else if (Array.isArray(dependency.licenses)) {
    const types = new Set(
      dependency.licenses
        .map((item) => (typeof item === "object" && item && item.type ? item.type : null))
        .filter(Boolean)
        .map((item) => applyOverrides(name, version, item))
    );
    license = [...types].sort().join(" OR ");
  }

  const licenseText = await readLicenseText(packageDirectory);

  return { license, licenseText, version };
};

module.exports = {
  getLicense,
};
