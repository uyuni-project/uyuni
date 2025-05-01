const fs = require("fs").promises;
const path = require("path");

const { getDependencyMap } = require("./package");
const { fileExists, getFileHash } = require("./fs");
const { template } = require("./template");

const dirname = path.dirname(__filename);
const webHtmlSrc = path.resolve(dirname, "../..");

const vendors = path.resolve(webHtmlSrc, "vendors");
const licenseTextFile = path.resolve(vendors, "npm.licenses.txt");
const licenseListFile = path.resolve(vendors, "npm.licenses.structured.js");
const hashFile = path.resolve(vendors, "npm.licenses.hash.txt");

(async () => {
  const licenseTextExists = await fileExists(licenseTextFile);
  const licenseListExists = await fileExists(licenseListFile);
  let previousHash;
  try {
    previousHash = await fs.readFile(hashFile, "utf8");
  } catch {}

  const currentHash = await getFileHash(path.resolve(webHtmlSrc, "yarn.lock"));
  if (previousHash && previousHash === currentHash && licenseTextExists && licenseListExists) {
    console.info("Skipping license check, hashes match");
    process.exitCode = 0;
    return;
  }

  try {
    const dependencies = await getDependencyMap(webHtmlSrc);

    const licenseTypes = Array.from(
      new Set(
        Object.values(dependencies)
          .flat()
          .map((entry) => entry.license)
          .sort()
          .filter(Boolean)
      )
    );
    await fs.writeFile(licenseListFile, `module.exports = ${JSON.stringify(licenseTypes)};`, "utf8");

    const lines = [];
    for (const name of Array.from(dependencies.keys()).sort()) {
      const entries = dependencies.get(name).sort((a, b) => a.version.localeCompare(b.version));
      for (const { version, licenseText } of entries) {
        lines.push(`${name}@${version}\n\n${licenseText}`);
      }
    }

    console.log(template(lines));

    // TODO: Reenable
    // await fs.writeFile(hashFile, currentHash, "utf8");
  } catch (error) {
    console.error(error);
    console.error("\nUnable to identify all licenses, did you run `yarn install`?\n");
    process.exitCode ||= 1;
  }
})();
