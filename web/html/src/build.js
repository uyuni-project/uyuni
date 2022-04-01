/* eslint-disable no-console */
const shell = require("shelljs");
const path = require("path");
const v8 = require("v8");
const webpack = require("webpack");
const { fillSpecFile } = require("./build/fill-spec-file");
const config = require("./build/webpack.config");

webpack(config(process.env, { mode: "production" }), (err, stats) => {
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

  const shouldValidateBuild = process.env.BUILD_VALIDATION !== "false";

  fillSpecFile().then(() => {
    if (shouldValidateBuild) {
      // Check whether the updated specfile and licenses are committed on git
      const rootDir = path.resolve(__dirname, "../../../");
      const { code: gitCheckCode, stdout } = shell.exec("git ls-files -m", {
        cwd: rootDir,
      });

      if (gitCheckCode !== 0) {
        process.exitCode = gitCheckCode;
        return;
      }

      const uncommittedFiles = editedLicenseFilesByBuild.filter((fileName) => stdout.includes(fileName));

      if (uncommittedFiles.length) {
        console.error(`
                It seems changes to license and/or spec files haven't been committed.
                Please run "yarn build" again and commit the following files: ${uncommittedFiles.join(", ")}`);
        process.exitCode = 1;
        return;
      }
    }
  });
});
