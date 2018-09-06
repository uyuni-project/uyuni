const shell = require('shelljs');
const { fillSpecFile } = require("./build/fill-spec-file");

const { code: codeVendors } = shell.exec("webpack --config build/webpack.config.vendors.js --mode production");
const { code: codeSource } = shell.exec("webpack --config build/webpack.config.js --mode production");
if (codeVendors !== 0 || codeSource !== 0) {
  shell.exit(codeVendors === 0 ? codeSource : codeVendors);
}

fillSpecFile()
  .then(() => {
    // let's make a sanity check if the generated specfile with the right  licenses is commited on git
    const { stdout } = shell.exec("(cd ../../;hash git && git ls-files -m)");

    if(stdout && stdout.includes("spacewalk-web.spec")) {
      throw new Error("It seems the most recent spacewalk-web.spec file isn't on git, run build again and commit the new generated susemanager-web-libs.spec file ");
    }

    //shell.exec("node build/check-undeclared-vendors.js");
  });
