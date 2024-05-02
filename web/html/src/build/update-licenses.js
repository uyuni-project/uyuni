const fs = require("fs");
const path = require("path");
const util = require("util");
const exec = util.promisify(require("child_process").exec);
const correct = require("spdx-correct");
const semver = require("semver");

const specFilePath = path.resolve(process.cwd(), "../../spacewalk-web.spec");
const licenseTextFilePath = path.resolve(process.cwd(), "./vendors/npm.licenses.txt");

// This function intentionally does not try-catch, we always want it to blow up if something is off
async function updateLicenses() {
  const pkg = JSON.parse(await fs.promises.readFile(path.resolve(process.cwd(), "package.json"), "utf8"));

  const { stdout, stderr } = await exec("yarn -s license-checker-rseidelsohn --json");
  if (stderr) {
    throw new Error(stderr);
  }

  // This prefix is copied over from the old license text file format
  const licenseTextFragments = [`THIRD PARTY SOFTWARE NOTICES AND INFORMATION\nDo NOT translate or localize\n`];
  const line = "-".repeat(80);
  const licenseSet = new Set();

  // Keep the original base license
  licenseSet.add("GPL-2.0-only");
  // Files under `web/html/javascript` are MIT licensed but won't be inferred from the automatic list
  licenseSet.add("MIT");

  const entries = JSON.parse(stdout);
  const seen = new Set();
  const sortedEntries = Object.entries(entries).sort(([a], [b]) => a.localeCompare(b));

  for (const [nameAndVersion, properties] of sortedEntries) {
    // This matches the old license checker plugin behavior
    const [name, version] = nameAndVersion.split("@");
    if (!pkg.dependencies[name] || seen.has(nameAndVersion)) {
      if (seen.has(nameAndVersion)) {
        console.log("seen:", nameAndVersion);
      }
      continue;
    }

    const expected = pkg.resolutions[name] || pkg.dependencies[name];
    const satisfies = semver.satisfies(version, expected);
    const coerced = semver.coerce(expected)?.version;
    const satisfiesCoerced = Boolean(coerced && semver.satisfies(version, coerced));
    if (!(satisfies || satisfiesCoerced)) {
      continue;
    }
    console.log("add:", nameAndVersion);
    seen.add(nameAndVersion);

    if (!(properties.licenses && properties.licenseFile)) {
      throw new Error(`Unable to find license and license file for dependency ${nameAndVersion}`);
    }

    licenseTextFragments.push(line);
    const publisherInfo = `${properties.publisher || ""} ${properties.email ? `<${properties.email}>` : ""}`.trim();
    licenseTextFragments.push(`${nameAndVersion}${publisherInfo ? ` - ${publisherInfo}` : ""}`);
    if (properties.repository) {
      licenseTextFragments.push(properties.repository);
    }
    licenseTextFragments.push(line);
    licenseTextFragments.push("");

    const file = await fs.promises.readFile(properties.licenseFile, "utf-8");
    licenseTextFragments.push(file);
    licenseTextFragments.push("");

    const corrected = correct(properties.licenses);
    // Ensure we don't end up with set items that include e.g. both "MIT" as well as "MIT AND FOO", leading to "MIT AND MIT AND FOO"
    if (!(corrected.includes("(") && corrected.includes(")")) && corrected.includes(" AND ")) {
      corrected.split(" AND ").forEach((item) => licenseSet.add(correct(item)));
    } else {
      licenseSet.add(corrected);
    }
  }

  const licenseText = licenseTextFragments.join("\n");
  await fs.promises.writeFile(licenseTextFilePath, licenseText, "utf-8");

  const licenses = Array.from(licenseSet).sort().join(" AND ");
  const currentSpecFile = await fs.promises.readFile(specFilePath, "utf-8");
  const editedSpecFile = currentSpecFile.replace(
    /(?<=%package -n spacewalk-html[\s\S]*?)License:.*/m,
    `License:        ${licenses}`
  );
  if (currentSpecFile === editedSpecFile) {
    console.log(`${path.basename(specFilePath)} is already up to date.`);
    return;
  }

  await fs.promises.writeFile(specFilePath, editedSpecFile, "utf-8");
  console.log(
    `${path.basename(
      specFilePath
    )} was updated successfully with the following licenses for spacewalk-html: ${licenses}`
  );
}

module.exports = {
  updateLicenses,
};
