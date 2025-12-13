import child_process from "node:child_process";
import path, { dirname } from "node:path";
import { fileURLToPath } from "node:url";
import util from "node:util";
import v8 from "v8";
import rawWebpack from "webpack";
import { hideBin } from "yargs/helpers";
import yargs from "yargs/yargs";

import checkPackage from "./build/check-package.js";
import { fillSpecFile } from "./build/fill-spec-file.js";
import { aggregateLicenses } from "./build/licenses/index.js";
import config from "./build/webpack/config.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const exec = util.promisify(child_process.exec);
const webpack = util.promisify(rawWebpack);

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

    const stats = await webpack(config(process.env, opts));
    if (opts.verbose) {
      console.log("webpack output:");
      console.log(stats?.toString());
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

    await fillSpecFile();

    if (opts.checkSpec) {
      const rootDir = path.resolve(__dirname, "../../../");
      const { code: gitCheckCode, stdout } = await exec("git ls-files -m", {
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
        const { stdout: diffOut } = await exec(`git diff -- ${editedLicenseFilesByBuild.join(" ")}`, {
          cwd: rootDir,
        });
        console.log(diffOut);

        console.error(`
                It seems changes to license and/or spec files haven't been committed.
                Please run "npm run build" again and commit the following files: ${uncommittedFiles.join(", ")}`);

        if (opts.force) {
          console.error(`WARN: Ignoring uncommitted spec changes because build was called with --force`);
        } else {
          process.exitCode = 1;
          return;
        }
      }
    }
  } catch (error) {
    console.error(error.stack || error);

    // See https://webpack.js.org/api/node/#error-handling
    if (error?.message?.toLowerCase().match(/\bmemory\b/)) {
      // See https://stackoverflow.com/a/38049633/1470607
      console.error("Process memory usage:");
      console.error(process.memoryUsage());
      console.error("V8 heap statistics:");
      console.error(v8.getHeapStatistics());
    }
    process.exitCode = 1;
  }
})();
