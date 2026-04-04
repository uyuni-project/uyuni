import { promises as fs } from "node:fs";
import path from "node:path";

import { applyOverrides } from "./override.js";

const readLicenseText = async (packageDirectory) => {
  const files = await fs.readdir(packageDirectory);
  const licenseFilename = files.find((name) => /^(license|licence|copying)/i.test(name));
  if (!licenseFilename) {
    return undefined;
  }
  return await fs.readFile(path.join(packageDirectory, licenseFilename), "utf8");
};

export const getLicense = async (dependency, packageDirectory) => {
  const name = dependency.name;
  const version = dependency.version;

  let license = "";

  if (typeof dependency.license === "string") {
    license = applyOverrides(name, version, dependency.license);
  } else if (Array.isArray(dependency.licenses)) {
    // Some legacy packages do not conform with the modern spec and keep license info as an array in package.json
    const types = new Set(
      dependency.licenses
        .map((item) => item?.type)
        .filter(Boolean)
        .map((item) => applyOverrides(name, version, item))
    );
    license = [...types].sort().join(" OR ");
  }

  const licenseText = await readLicenseText(packageDirectory);

  return { license, licenseText, version };
};
