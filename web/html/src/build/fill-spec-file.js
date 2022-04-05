const fs = require("fs");
const path = require("path");

function fillSpecFile() {
  delete require.cache[require.resolve("../vendors/npm.licenses.structured")];
  const { npmLicensesArray } = require("../vendors/npm.licenses.structured");

  // Keep the original base license
  npmLicensesArray.push("GPL-2.0-only");
  // Files under `web/html/javascript` are MIT licensed but won't be inferred from the automatic list
  npmLicensesArray.push("MIT");

  //https://github.com/metal/metal.js/issues/411
  const processedLicenses = npmLicensesArray.map((item) => {
    if (item === "BSD") {
      return "0BSD";
    }
    return item;
  });
  const mappedProcessedLicenses = Array.from(new Set(processedLicenses)).sort().join(" AND ");

  const specFileLocation = "../../spacewalk-web.spec";

  return new Promise(function (resolve, reject) {
    fs.readFile(specFileLocation, "utf8", function (err, specFile) {
      if (err) {
        reject(err);
        throw err;
      }
      const editedSpecFile = specFile.replace(
        /(?<=%package -n spacewalk-html[\s\S]*?)License:.*/m,
        `License:        ${mappedProcessedLicenses}`
      );

      if (editedSpecFile === specFile) {
        console.log(`${path.basename(specFileLocation)} is already up to date.`);
        resolve();
        return;
      }

      fs.writeFile(specFileLocation, editedSpecFile, "utf8", function (err) {
        if (err) {
          reject(err);
          throw err;
        }

        console.log(
          `${path.basename(
            specFileLocation
          )} was updated successfully with the following licenses for spacewalk-html: ${mappedProcessedLicenses}`
        );
        resolve();
      });
    });
  });
}

module.exports = {
  fillSpecFile,
};
