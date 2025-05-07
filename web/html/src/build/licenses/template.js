const separator = "\n--------------------------------------------------------------------------------\n";

const itemTemplate = (name, version, licenseText) => `
Package: ${name}@${version}
License text:

${licenseText}`;

const fileTemplate = (licenseTypes) => `THIRD PARTY SOFTWARE NOTICES AND INFORMATION
Do NOT translate or localize
${separator}${licenseTypes.join(separator)}`;

module.exports = { itemTemplate, fileTemplate };
