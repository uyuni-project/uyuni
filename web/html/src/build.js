const shell = require("shelljs");
const path = require("path");
const v8 = require("v8");
const webpack = require("webpack");
const yargs = require("yargs/yargs");
const { hideBin } = require("yargs/helpers");

const { fillSpecFile } = require("./build/fill-spec-file");
const config = require("./build/webpack.config");
const checkPackage = require("./build/check-package");
const { aggregateLicenses } = require("./build/licenses");

const opts = yargs(hideBin(process.argv))
  .version(false)
  .wrap(null)
  .option("check-package-json", {
    type: "boolean",
    description:
      "Run sanity checks for package.json and ensure that all directly defined dependencies have correct versions",
    default: true,
  })
  .option("check-spec", {
    type: "boolean",
    description: "Check whether the specfile is up to date with the latest licenses and committed on git",
    default: true,
  })
  .option("force", {
    type: "boolean",
    description: "Force rebuild, ignore caches, ignore sanity checks",
    default: false,
  })
  .option("measure-performance", {
    type: "boolean",
    description: "Measure and output build performance statistics",
    default: false,
  })
  .option("mode", {
    type: "string",
    description: "Webpack build mode (see https://webpack.js.org/configuration/mode/)",
    choices: ["production", "development"],
    default: "production",
  })
  .option("verbose", {
    type: "boolean",
    default: false,
  })
  .parse();

if (opts.verbose) {
  console.log("verbose output enabled");
  console.log("build flags:");
  console.log(opts);
}

(async () => {
  try {
    if (opts.checkPackageJson) {
      await checkPackage(opts);
    }

    if (opts.mode === "production") {
      await aggregateLicenses(opts);
    }

    webpack(config(process.env, opts), (err, stats) => {
      if (opts.verbose) {
        console.log("webpack output:");
        console.log(stats?.toString());
      }

      // See https://webpack.js.org/api/node/#error-handling
      if (err) {
        console.error("Webpack error");
        console.error(err.stack || err);
        if (err.message.toLowerCase().match(/\bmemory\b/)) {
          // See https://stackoverflow.com/a/38049633/1470607
          console.error("Process memory usage:");
          console.error(process.memoryUsage());
          console.error("V8 heap statistics:");
          console.error(v8.getHeapStatistics());
        }
        process.exitCode = err.code || 1;
        return;
      }

      if (stats.hasErrors()) {
        console.error("Build error");
        console.error(stats.toJson().errors);
        process.exitCode = stats.code || 1;
        return;
      }

      const editedLicenseFilesByBuild = [
        "web/spacewalk-web.spec",
        "web/html/src/vendors/npm.licenses.structured.js",
        "web/html/src/vendors/npm.licenses.txt",
      ];

      fillSpecFile().then(() => {
        if (opts.checkSpec) {
          const rootDir = path.resolve(__dirname, "../../../");
          const { code: gitCheckCode, stdout } = shell.exec("git ls-files -m", {
            cwd: rootDir,
          });

          if (opts.verbose) {
            console.log(stdout);
          }

          if (gitCheckCode !== 0) {
            process.exitCode = gitCheckCode;
            return;
          }

          const uncommittedFiles = editedLicenseFilesByBuild.filter((fileName) => stdout.includes(fileName));

          if (uncommittedFiles.length) {
            console.error(`
                    It seems changes to license and/or spec files haven't been committed.
                    Please run "yarn build" again and commit the following files: ${uncommittedFiles.join(", ")}`);
            if (opts.force) {
              console.error(`WARN: Ignoring uncommitted spec changes because build was called with --force`);
            } else {
              process.exitCode = 1;
              return;
            }
          }
        }
      });
    });
  } catch (error) {
    console.error(error);
    process.exitCode = 1;
  }
})();
