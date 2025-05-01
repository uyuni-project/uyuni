const separator = "\n\n--------------------------------------------------------------------------------\n\n";

const template = (licenseTypes) => `THIRD PARTY SOFTWARE NOTICES AND INFORMATION
Do Not Translate or Localize

${separator}
${licenseTypes.join(separator)}`;

module.exports = { template, separator };
