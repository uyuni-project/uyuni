const fs = require("fs");

function fillSpecFile() {
    delete require.cache[require.resolve("../vendors/npm.licenses.structured")];
    const {npmLicensesArray} =  require("../vendors/npm.licenses.structured");
    const processedLicenses = [...new Set(npmLicensesArray)].sort().join(" and ");
    //https://github.com/metal/metal.js/issues/411
    const mappedProcessedLicenses = processedLicenses.replace("BSD", "0BSD");

    const specFileLocation = "../../spacewalk-web.spec";

    return new Promise(function(resolve, reject) {
        fs.readFile(specFileLocation, 'utf8', function (err, specFile) {
            if (err) {
                throw err;
            }
            var specFileEdited = specFile.replace(/(?<=%package -n susemanager-web-libs[\s\S]*?)License:.*/m, `License:\t\t${mappedProcessedLicenses}`);

            fs.writeFile(specFileLocation, specFileEdited, 'utf8', function (err) {
                if (err) {
                    throw err;
                }

                resolve({mappedProcessedLicenses});
                console.log(`susemanager-web-libs.spec was generated successfully with the following licenses: ${mappedProcessedLicenses}`);
            });

        });
    });
}

module.exports = {
    fillSpecFile
}
