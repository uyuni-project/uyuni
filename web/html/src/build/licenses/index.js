const { promises: fs } = require("fs");
const path = require("path");

const { getDependencies } = require("./yarn");
const { applyOverrides } = require("./override");
const { getPackageName, buildPackageMap } = require("./package");

const dirname = path.dirname(__filename);
const webHtmlSrc = path.resolve(dirname, "../..");

(async () => {
  try {
    const dependencies = await getDependencies();
    const licenseInfo = {};
    const packageMap = await buildPackageMap(path.join(webHtmlSrc, "node_modules"));

    await Promise.all(
      dependencies.map(async (fullName) => {
        const name = getPackageName(fullName);
        const packageJsonPath = packageMap.get(name);

        if (packageJsonPath) {
          try {
            const pkgJson = await fs.readFile(packageJsonPath, "utf8");
            const pkg = JSON.parse(pkgJson);
            let license = "";
            if (typeof pkg.license === "string") {
              license = applyOverrides(name, pkg.license);
            } else if (Array.isArray(pkg.licenses)) {
              const types = new Set(
                pkg.licenses
                  .map((item) => (typeof item === "object" && item && item.type ? item.type : null))
                  .filter(Boolean)
                  .map((item) => applyOverrides(name, item))
              );
              license = [...types].sort().join(" OR ");
            }
            if (license) {
              licenseInfo[name] = applyOverrides(name, license);
            }
          } catch {
            licenseInfo[name] = "MISSING";
            process.exitCode = 1;
          }
        } else {
          licenseInfo[name] = "MISSING";
          process.exitCode = 1;
        }
      })
    );

    console.log(licenseInfo);

    console.log(new Set(Object.values(licenseInfo)));

    if (process.exitCode) {
      console.error(licenseInfo);
      console.error("Unable to identify all licenses, did you run `yarn install`?");
      return;
    }
  } catch (error) {
    console.error(error);
    process.exitCode ||= 1;
  }
})();
