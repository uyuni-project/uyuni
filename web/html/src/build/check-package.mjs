/**
 * Basic sanity check for package.json, check that all directly defined dependencies have correct versions.
 * This avoids issues with mismatched packages when building patches or working in OBS/IBS.
 */

/* eslint-disable no-console */
import fs from "fs";
import path from "path";
import semver from "semver";

(async () => {
  try {
    const cwd = process.cwd();
    const pkg = JSON.parse(await fs.promises.readFile(path.resolve(cwd, "package.json"), "utf8"));

    // Currently we only check production dependencies
    const dependencies = pkg.dependencies;
    // TODO: Do we need to also support resolutions?
    const resolutions = pkg.resolutions;

    const nodeModules = path.resolve(cwd, "node_modules");
    for (const [dependency, expectedVersion] of Object.entries(dependencies)) {
      const installedPkg = JSON.parse(
        await fs.promises.readFile(path.resolve(nodeModules, dependency, "package.json"), "utf8")
      );

      const satisfies = semver.satisfies(installedPkg.version, expectedVersion);
      const satisfiesCoerced = semver.satisfies(installedPkg.version, semver.coerce(expectedVersion).version);
      if (!satisfies && satisfiesCoerced) {
        console.warn(
          `WARN: ${dependency} version ${installedPkg.version} only matches ${expectedVersion} when coerced`
        );
      }

      if (!satisfies && !satisfiesCoerced) {
        console.error(`ERROR: ${dependency} version ${installedPkg.version} does not satisfy ${expectedVersion}`);
        process.exitCode = 1;
      }
    }
  } catch (error) {
    console.error(error);
    process.exitCode = 1;
  }
})();
