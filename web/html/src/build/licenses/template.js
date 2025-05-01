const separator = "\n\n--------------------------------------------------------------------------------\n\n";

const template = (licenseTypes) => `THIRD PARTY SOFTWARE NOTICES AND INFORMATION
Do NOT translate or localize

${separator}
${licenseTypes.join(separator)}`;

module.exports = { template, separator };
