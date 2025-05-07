const fs = require("fs").promises;
const path = require("path");

const { getDependencyMap } = require("./package");
const { fileExists, getFileHash } = require("./fs");
const { fileTemplate, itemTemplate } = require("./template");

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
  } catch {
    // Do nothing
  }

  const currentHash = await getFileHash(path.resolve(webHtmlSrc, "yarn.lock"));
  if (previousHash && previousHash === currentHash && licenseTextExists && licenseListExists) {
    console.info("Skipping license check, hashes match");
    process.exitCode = 0;
    return;
  }

  try {
    const dependencies = await getDependencyMap(webHtmlSrc);

    // Aggregate all available license texts
    const lines = Array.from(dependencies.keys())
      .sort()
      .flatMap((name) =>
        dependencies
          .get(name)
          .sort((a, b) => a.version.localeCompare(b.version))
          // Not all packages include the license text in the distribution, even when they should
          .filter((item) => typeof item.licenseText !== "undefined")
          .map((item) => itemTemplate(name, item.version, item.licenseText))
      );
    await fs.writeFile(licenseTextFile, fileTemplate(lines), "utf8");

    // Get a list of all licenses to populate `spacewalk-web.spec`
    const licenseTypes = new Set(
      Array.from(dependencies.values())
        .flat()
        .map((item) => item.license)
        .filter(Boolean)
        .sort((a, b) => a.localeCompare(b))
    );
    console.log(licenseTypes.keys());
    await fs.writeFile(licenseListFile, `module.exports = ${JSON.stringify(licenseTypes)};`, "utf8");

    // TODO: Reenable
    // await fs.writeFile(hashFile, currentHash, "utf8");
  } catch (error) {
    console.error(error);
    console.error("\nUnable to identify all licenses, did you run `yarn install`?\n");
    process.exitCode ||= 1;
  }
})();
