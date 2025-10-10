import { promises as fs } from "node:fs";
import path, { dirname } from "node:path";
import { fileURLToPath } from "node:url";

import { getFileHash, isValidLicenseFile } from "./fs.js";
import { getDependencyMap } from "./package.js";
import { fileTemplate, itemTemplate } from "./template.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const webHtmlSrc = path.resolve(__dirname, "../..");

const vendors = path.resolve(webHtmlSrc, "vendors");
const licenseTextFile = path.resolve(vendors, "npm.licenses.txt");
const licenseListFile = path.resolve(vendors, "npm.licenses.structured.js");
const hashFile = path.resolve(vendors, "npm.licenses.hash.txt");

export async function aggregateLicenses(opts) {
  const licenseTextExists = await isValidLicenseFile(licenseTextFile);
  const licenseListExists = await isValidLicenseFile(licenseListFile);

  let previousHash;
  try {
    previousHash = await fs.readFile(hashFile, "utf8");
  } catch {
    // Do nothing
  }

  const currentHash = await getFileHash(path.resolve(webHtmlSrc, "yarn.lock"));
  if (opts.force !== true && previousHash && previousHash === currentHash && licenseTextExists && licenseListExists) {
    console.info("Skipping license check, hashes match");
    return;
  }

  try {
    const dependencies = await getDependencyMap(webHtmlSrc);

    // Aggregate all available license texts into `web/html/src/vendors/npm.licenses.txt`
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

    // Aggregate a list of all unique licenses into `web/html/src/vendors/npm.licenses.structured.js`, other build tooling uses this to populate `spacewalk-web.spec`
    const licenseTypes = [
      ...new Set(
        Array.from(dependencies.values())
          .flat()
          // If the license is "X AND Y", append "X" and "Y" into the individual license types separately to dedupe them
          .flatMap((item) => item?.license?.split(" AND "))
          .filter(Boolean)
          .sort((a, b) => a.localeCompare(b))
      ),
    ];
    await fs.writeFile(licenseListFile, `export default ${JSON.stringify(licenseTypes)};`, "utf8");

    await fs.writeFile(hashFile, currentHash, "utf8");
  } catch (error) {
    console.error(error);
    throw new Error("Unable to identify all licenses, did you run `yarn install`?");
  }
}
