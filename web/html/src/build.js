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

  // These are relative to the web folder
  const editedLicenseFilesByBuild = [
    "spacewalk-web.spec",
    "html/src/vendors/npm.licenses.structured.js",
    "html/src/vendors/npm.licenses.txt",
  ];

  const shouldValidateBuild = process.env.BUILD_VALIDATION !== "false";

  fillSpecFile().then(() => {
    if (shouldValidateBuild) {
      // Check whether the updated specfile and licenses are committed on git
      const webDir = path.resolve(__dirname, "../../");
      const { code: gitCheckCode, stdout } = shell.exec("git ls-files -m", {
        cwd: webDir,
      });
      if (gitCheckCode !== 0) {
        process.exitCode = gitCheckCode;
        return;
      }

      if (stdout && editedLicenseFilesByBuild.some((fileName) => stdout.includes(fileName))) {
        console.error(`
                It seems the most recent ${editedLicenseFilesByBuild} files aren't on git.
                Run "yarn build" again and commit the following files: ${editedLicenseFilesByBuild.join(", ")}`);
        // TODO: This should be an error again after dependabot issues are addressed
        // process.exitCode = 1;
        // return;
      }

      // TODO: This should be simply `yarn audit` once Storybook issues are resolved
      const { stdout: auditStdout } = shell.exec("yarn audit --groups dependencies,devDependencies");

      if (auditStdout && !auditStdout.includes("0 vulnerabilities found")) {
        console.error(`
                There are vulnerabilities on the downloaded npm libraries.
                Please run "yarn audit" and fix the detected vulnerabilities `);
        process.exitCode = 1;
        return;
      }
    }
  });
});
