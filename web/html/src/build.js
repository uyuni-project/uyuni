const shell = require('shelljs');
const { fillSpecFile } = require("./build/fill-spec-file");

const { code: codeBuild } = shell.exec("webpack --config build/webpack.config.js --mode production");
if (codeBuild !== 0) {
  shell.exit(codeBuild);
}

const editedLicenseFilesByBuild = [
  "spacewalk-web.spec",
  "vendors/npm.licenses.structured.js",
  "vendors/npm.licenses.txt"
];

const shouldValidateBuild = process.env.BUILD_VALIDATION !== "false";


fillSpecFile()
  .then(() => {
    if(shouldValidateBuild) {
      // let's make a sanity check if the generated specfile with the right  licenses is commited on git
      const {code: gitCheckCode, stdout} = shell.exec("(cd ../../;git ls-files -m)");
      if (gitCheckCode !== 0) {
        shell.exit(gitCheckCode);
      }

      if (stdout && editedLicenseFilesByBuild.some(fileName => stdout.includes(fileName))) {
        shell.echo(`
                It seems the most recent ${editedLicenseFilesByBuild} files aren't on git.
                Run "yarn build" again and commit the generated ${editedLicenseFilesByBuild} files `);
        shell.exit(1);
      }

      const {stdout: auditStdout} = shell.exec("yarn audit");

      if (auditStdout && !auditStdout.includes("0 vulnerabilities found")) {
        shell.echo(`
                There are vulnerabilities on the downloaded npm libraries.
                Please run "yarn audit" and fix the detected vulnerabilities `);
        shell.exit(1);
      }
    }
  });
