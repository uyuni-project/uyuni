const { promises: fs } = require("fs");
const path = require("path");

const { getDependencies } = require("./yarn");
const { applyOverrides } = require("./override");
const { getPackageName, buildPackageMap } = require("./package");
const { fileExists, getFileHash } = require("./fs");
const { template } = require("./template");

const dirname = path.dirname(__filename);
const webHtmlSrc = path.resolve(dirname, "../..");

const vendors = path.resolve(webHtmlSrc, "vendors");
// TODO: Mark these as generated files
const licenseTextFile = path.resolve(vendors, "npm.licenses.txt");
const licenseListFile = path.resolve(vendors, "npm.licenses.structured.js");
const hashFile = path.resolve(vendors, "npm.licenses.hash.txt");

(async () => {
  // If all the required files exist and no dependencies have changed, we have nothing to do
  const licenseTextFileExists = await fileExists(licenseTextFile);
  const licenseListFileExists = await fileExists(licenseListFile);
  let previousHash = undefined;
  try {
    previousHash = await fs.readFile(hashFile, "utf8");
  } catch {
    // Do nothing
  }

  const currentHash = await getFileHash(path.resolve(webHtmlSrc, "yarn.lock"));
  if (previousHash && previousHash === currentHash && licenseTextFileExists && licenseListFileExists) {
    console.info("Skipping license check, hashes match");
    process.exitCode = 0;
    return;
  }

  /**
   * NB! We intentionally do NOT try-catch errors beyond this point so we blow up if some data is missing
   */
  try {
    const dependencies = await getDependencies();
    const licenseInfo = {};
    const packageMap = await buildPackageMap(path.join(webHtmlSrc, "node_modules"));

    await Promise.all(
      dependencies.map(async (fullName) => {
        const packageName = getPackageName(fullName);
        const packageJsonPaths = packageMap.get(packageName);

        if (!packageJsonPaths || packageJsonPaths.length === 0) {
          throw new RangeError(`Unable to find package.json for "${fullName}"`);
        }

        const infos = await Promise.all(
          packageJsonPaths.map(async (packageJsonPath) => {
            const pkgJson = await fs.readFile(packageJsonPath, "utf8");
            const pkg = JSON.parse(pkgJson);
            const version = pkg.version;
            if (typeof version === "undefined") {
              throw new RangeError(`Unable to identify package version for "${fullName}"`);
            }

            let license = "";
            if (typeof pkg.license === "string") {
              license = applyOverrides(packageName, version, pkg.license);
            } else if (Array.isArray(pkg.licenses)) {
              const types = new Set(
                pkg.licenses
                  .map((item) => (typeof item === "object" && item && item.type ? item.type : null))
                  .filter(Boolean)
                  .map((item) => applyOverrides(packageName, version, item))
              );
              license = [...types].sort().join(" OR ");
            }

            const pkgDir = path.dirname(packageJsonPath);
            const files = await fs.readdir(pkgDir);
            const licenseFile = files.find((f) => /^(license|licence|copying)/i.test(f));

            // Many packages do not include license texts in their distribution, even though they should
            let licenseText = "";
            if (licenseFile) {
              licenseText = await fs.readFile(path.join(pkgDir, licenseFile), "utf8");
            }

            return { license, licenseText, version };
          })
        );

        // Dedupe if package name, version and license text match, e.g. the same package and version is included multiple times in the tree
        const unique = [];
        const seen = new Set();
        for (const entry of infos) {
          const key = JSON.stringify(entry);
          if (!seen.has(key)) {
            seen.add(key);
            unique.push(entry);
          }
        }

        licenseInfo[packageName] = unique;
      })
    );

    const licenseTypes = Array.from(
      new Set(
        Object.values(licenseInfo)
          .flat()
          .map((info) => info.license)
          .sort()
          .filter(Boolean)
      )
    );
    await fs.writeFile(licenseListFile, `module.exports = ${JSON.stringify(licenseTypes)};`, "utf8");

    console.log(licenseInfo);

    // TODO: Reenable
    // Only once everything else is done, update the hash
    // await fs.writeFile(hashFile, currentHash, "utf8");
  } catch (error) {
    console.error(error);
    console.error("\nUnable to identify all licenses, did you run `yarn install`?\n");
    process.exitCode ||= 1;
  }
})();
