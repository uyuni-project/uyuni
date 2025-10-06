import { promises as fs } from "node:fs";
import path from "node:path";

import { getLicense } from "./license.js";

const resolveDependencyPath = (packageName, baseDirectory) => {
  const parts = packageName.startsWith("@") ? packageName.split("/") : [packageName];
  return path.join(baseDirectory, "node_modules", ...parts);
};

// Packages may be nested in subdependencies or hoisted up, try and look everywhere reasonable and then throw if we still don't find a match
const findPackageJson = async (packageName, searchStartDirectory) => {
  let currentDirectory = searchStartDirectory;
  while (true) {
    const packageDirectory = resolveDependencyPath(packageName, currentDirectory);
    try {
      const json = await fs.readFile(path.join(packageDirectory, "package.json"), "utf8");
      return {
        packageJson: JSON.parse(json),
        packageDirectory,
      };
    } catch {
      const parentDirectory = path.dirname(currentDirectory);
      if (parentDirectory === currentDirectory) {
        break;
      }
      currentDirectory = parentDirectory;
    }
  }
  throw new TypeError(`Unable to find package.json for "${packageName}" in "${searchStartDirectory}"`);
};

// Get a map of all production dependencies and their license information
export const getDependencyMap = async (rootDirectory) => {
  const visited = new Set();
  const licenseMap = new Map();

  const walk = async (packageName, baseDirectory) => {
    const { packageJson, packageDirectory } = await findPackageJson(packageName, baseDirectory);

    if (!packageJson.name || !packageJson.version) {
      throw new RangeError(`Invalid package.json for "${packageName}"`);
    }

    const uniqueKey = `${packageJson.name}@${packageJson.version}`;
    if (visited.has(uniqueKey)) {
      return;
    }
    visited.add(uniqueKey);

    const license = await getLicense(packageJson, packageDirectory);

    if (!licenseMap.has(packageJson.name)) {
      licenseMap.set(packageJson.name, []);
    }

    const existing = licenseMap.get(packageJson.name);
    const entryKey = JSON.stringify(license);
    if (!existing.some((e) => JSON.stringify(e) === entryKey)) {
      existing.push(license);
    }

    const dependencies = Object.keys(packageJson.dependencies || {});
    await Promise.all(dependencies.map((name) => walk(name, packageDirectory)));
  };

  const rootPackageJson = JSON.parse(await fs.readFile(path.join(rootDirectory, "package.json"), "utf8"));
  const rootDependencies = Object.keys(rootPackageJson.dependencies || {});
  await Promise.all(rootDependencies.map((name) => walk(name, rootDirectory)));

  return licenseMap;
};
