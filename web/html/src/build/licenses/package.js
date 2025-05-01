const { promises: fs } = require("fs");
const path = require("path");

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
            const pkgJson = await fs.readFile(packageJsonPath, "utf8");
            const pkg = JSON.parse(pkgJson);
            if (pkg.name) {
              if (!packageMap.has(pkg.name)) {
                packageMap.set(pkg.name, []);
              }
              packageMap.get(pkg.name).push(packageJsonPath);
            }
          }
        } catch {
          // Ignore missing or invalid package.json
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
