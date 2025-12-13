const separator = "\n--------------------------------------------------------------------------------\n";

export const itemTemplate = (name, version, licenseText) => `
Package: ${name}@${version}
License text:

${licenseText}`;

export const fileTemplate = (licenseTypes) => `THIRD PARTY SOFTWARE NOTICES AND INFORMATION
Do NOT translate or localize
${separator}${licenseTypes.join(separator)}`;
