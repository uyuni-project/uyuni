/**
 * Basic sanity check for package.json, check that all directly defined dependencies have correct versions.
 * This avoids issues with mismatched packages when building patches or working in OBS/IBS.
 */

/* eslint-disable no-console */
const fs = require("fs");
const path = require("path");
const semver = require("semver");

module.exports = async () => {
  let hasFailed = false;

  const dirname = path.dirname(__filename);
  const webHtmlSrc = path.resolve(dirname, "..");
  const pkg = JSON.parse(await fs.promises.readFile(path.resolve(webHtmlSrc, "package.json"), "utf8"));

  // Currently we only check production dependencies
  const dependencies = pkg.dependencies;
  // If we ever need to support resolutions, they're available here:
  // const resolutions = pkg.resolutions;

  const nodeModules = path.resolve(webHtmlSrc, "node_modules");
  for (const [dependency, expectedVersion] of Object.entries(dependencies)) {
    const installedPkg = JSON.parse(
      await fs.promises.readFile(path.resolve(nodeModules, dependency, "package.json"), "utf8")
    );

    const satisfies = semver.satisfies(installedPkg.version, expectedVersion);
    const satisfiesCoerced = semver.satisfies(installedPkg.version, semver.coerce(expectedVersion).version);
    if (!satisfies && satisfiesCoerced) {
      console.warn(
        `WARN: Currently installed ${dependency} version ${installedPkg.version} only matches expected version ${expectedVersion} when coerced`
      );
    }

    if (!satisfies && !satisfiesCoerced) {
      hasFailed = true;
      console.error(
        `ERROR: Currently installed ${dependency} version ${installedPkg.version} does not satisfy expected version ${expectedVersion}`
      );
    }
  }
  if (hasFailed) {
    throw new RangeError("Have you installed the latest dependencies? Try running: yarn install");
  }
};
