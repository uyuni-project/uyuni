const { promises: fs } = require("fs");
const path = require("path");

// Yarn outputs dependency names in format `name@version`, e.g. `foo@1.2.3`, but scoped package names include an @ at the start, e.g. `@foo/bar@1.2.3`.
const getPackageName = (fullName) => {
  if (fullName.startsWith("@")) {
    return fullName.split("@").slice(0, 2).join("@");
  }
  return fullName.split("@")[0];
};

const buildPackageMap = async (startDirectory) => {
  const packageMap = new Map();

  async function visit(directory) {
    let entries;
    try {
      entries = await fs.readdir(directory, { withFileTypes: true });
    } catch {
      return;
    }

    await Promise.all(
      entries.map(async (entry) => {
        if (!entry.isDirectory()) return;

        const entryPath = path.join(directory, entry.name);
        const packageJsonPath = path.join(entryPath, "package.json");

        try {
          const stat = await fs.stat(packageJsonPath);
          if (stat.isFile()) {
            try {
              const pkgJson = await fs.readFile(packageJsonPath, "utf8");
              const pkg = JSON.parse(pkgJson);
              if (pkg.name) {
                packageMap.set(pkg.name, packageJsonPath);
              }
            } catch {
              // Do nothing
            }
          }
        } catch {
          // Do nothing
        }

        await visit(entryPath);
      })
    );
  }

  await visit(startDirectory);
  return packageMap;
};

module.exports = {
  getPackageName,
  buildPackageMap,
};
