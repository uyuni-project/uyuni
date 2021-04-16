const fs = require("fs");

function fillSpecFile() {
    delete require.cache[require.resolve("../vendors/npm.licenses.structured")];
    const {npmLicensesArray} =  require("../vendors/npm.licenses.structured");
    //https://github.com/metal/metal.js/issues/411
    const processedLicenses = npmLicensesArray.map(item => {
        if (item === 'BSD') {
            return '0BSD';
        }
        return item;
    });
    const mappedProcessedLicenses = Array.from(new Set(processedLicenses)).sort().join(" and ");

    const specFileLocation = "../../spacewalk-web.spec";

    return new Promise(function(resolve, reject) {
        fs.readFile(specFileLocation, 'utf8', function (err, specFile) {
            if (err) {
                throw err;
            }
            var specFileEdited = specFile.replace(/(?<=%package -n susemanager-web-libs[\s\S]*?)License:.*/m, `License:        ${mappedProcessedLicenses}`);

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
