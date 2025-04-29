const { exec } = require("child_process");
const { promises: fs } = require("fs");
const path = require("path");

const yarnCwd = path.join("web", "html", "src");

function extractPackageName(fullName) {
  if (fullName.startsWith("@")) {
    return fullName.split("@").slice(0, 2).join("@");
  }
  return fullName.split("@")[0];
}

async function buildPackageMap(startDir) {
  const packageMap = new Map();

  async function recurse(dir) {
    let entries;
    try {
      entries = await fs.readdir(dir, { withFileTypes: true });
    } catch {
      return;
    }

    await Promise.all(entries.map(async (entry) => {
      if (!entry.isDirectory()) return;

      const entryPath = path.join(dir, entry.name);
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

      await recurse(entryPath);
    }));
  }

  await recurse(startDir);
  return packageMap;
}

exec("yarn list --production --json", async (err, stdout) => {
  if (err) {
    console.error(err);
    return;
  }
  const tree = stdout
    .split("\n")
    .filter(Boolean)
    .map((line) => JSON.parse(line))
    .find((line) => line.type === "tree");

  if (!tree) {
    console.error("No tree data found");
    return;
  }

  const flat = [];

  function walk(node) {
    flat.push(node.name);
    if (node.children) {
      node.children.forEach(walk);
    }
  }

  tree.data.trees.forEach(walk);

  const licenseInfo = {};

  const packageMap = await buildPackageMap(path.join(yarnCwd, "node_modules"));

  await Promise.all(flat.map(async (fullName) => {
    const name = extractPackageName(fullName);
    const packageJsonPath = packageMap.get(name);

    if (packageJsonPath) {
      try {
        const pkgJson = await fs.readFile(packageJsonPath, "utf8");
        const pkg = JSON.parse(pkgJson);
        let license = "";
        if (typeof pkg.license === "string") {
          license = pkg.license;
        } else if (Array.isArray(pkg.licenses)) {
          const types = new Set(
            pkg.licenses.map((l) => (typeof l === "object" && l && l.type ? l.type : null)).filter(Boolean)
          );
          console.log(types);
          license = [...types].sort().join(" AND ");
        }
        licenseInfo[name] = license;
      } catch {
        licenseInfo[name] = "MISSING";
      }
    } else {
      licenseInfo[name] = "MISSING";
    }
  }));

  console.log(licenseInfo);

  console.log(new Set(Object.values(licenseInfo)));
});
