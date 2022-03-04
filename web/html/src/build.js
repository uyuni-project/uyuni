const shell = require("shelljs");
const path = require("path");
const { fillSpecFile } = require("./build/fill-spec-file");

const { code: codeBuild } = shell.exec("webpack --config build/webpack.config.js --mode production");
if (codeBuild !== 0) {
  shell.exit(codeBuild);
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
      shell.exit(gitCheckCode);
    }

    if (stdout && editedLicenseFilesByBuild.some((fileName) => stdout.includes(fileName))) {
      shell.echo(`
                It seems the most recent ${editedLicenseFilesByBuild} files aren't on git.
                Run "yarn build" again and commit the following files: ${editedLicenseFilesByBuild.join(", ")}`);
      // TODO: This should be an error again after dependabot issues are addressed
      // shell.exit(1);
    }
  }
});
