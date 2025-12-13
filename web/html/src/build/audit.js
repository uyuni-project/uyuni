import { exec } from "child_process";

import ignore from "../.auditignore.js";

// npm audit doesn't currently support muting known issues nor issues with no fix available
exec(`npm audit --json --omit=dev --dry-run`, (_, stdout) => {
  try {
    const result = JSON.parse(stdout);
    if (result.auditReportVersion !== 2) {
      throw new RangeError("Unknown audit report version");
    }

    const vulnerabilities = Object.values(result.vulnerabilities);

    const validVulnerabilities = vulnerabilities.filter((item) => {
      const { name: moduleName, via: advisories } = item;
      if (ignore[moduleName]) {
        const urls = (advisories || []).map((item) => item.url).filter(Boolean);
        console.info(
          `Warning: Ignoring advisories for module "${moduleName}"${urls.length ? ": " : ""}${urls.join(", ")}\nReason: "${ignore[moduleName]}"`
        );
        delete ignore[moduleName];
        return false;
      }
      return true;
    });

    const unusedIgnores = Object.keys(ignore);
    if (unusedIgnores.length) {
      process.exitCode = 1;
      unusedIgnores.forEach((moduleName) => {
        console.error(`Error: Unused ignore for module "${moduleName}", please check .auditignore.js`);
      });
    }

    if (validVulnerabilities.length) {
      process.exitCode = 1;
      validVulnerabilities.forEach((item) => {
        const { name: moduleName, via: advisories } = item;
        const urls = advisories?.map((item) => item.url).filter(Boolean);
        console.error(`Error: Found advisories for module "${moduleName}": ${urls.join(", ")}`);
      });
    }
  } catch (error) {
    process.exitCode = error.code || 1;
    console.error(error);
  }
});
