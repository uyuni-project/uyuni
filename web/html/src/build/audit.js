const { exec } = require("child_process");

const ignore = require("../.auditignore.js");

const inline = (message) => {
  return (message || "").replaceAll(/\r?\n/g, " ");
};

// Yarn 1.x doesn't currently support muting known issues, see https://github.com/yarnpkg/yarn/issues/6669
exec(`yarn audit --json --groups "dependencies"`, (_, stdout) => {
  try {
    const lines = (stdout || "").split(/\r?\n/).filter((line) => line.trim() !== "");
    const results = lines.map((line) => JSON.parse(line));

    if (!results.some((item) => item.type === "auditSummary")) {
      throw new TypeError("No audit result found");
    }

    const advisories = results.filter((item) => item.type === "auditAdvisory");

    const validAdvisories = advisories.filter((item) => {
      const { module_name: moduleName, id, overview, recommendation } = item.data.advisory;
      if (ignore[moduleName]) {
        console.info(
          `Warning: Ignoring advisory ${id} for module "${moduleName}"\n\tOverview: ${inline(
            overview
          )}\n\tRecommendation: ${inline(recommendation)}\n\tReason for ignoring: ${ignore[moduleName]}\n`
        );
        delete ignore[moduleName];
        return false;
      }
      return true;
    });

    const unusedIgnores = Object.keys(ignore);
    if (unusedIgnores.length) {
      process.exitCode = 1;
      unusedIgnores.forEach((item) => {
        console.error(`Error: Unused ignore for module "${item}", please check .auditignore.js`);
      });
    }

    if (validAdvisories.length) {
      process.exitCode = 1;
      validAdvisories.forEach((item) => {
        const { module_name: moduleName, id, overview, recommendation } = item.data.advisory;
        console.error(
          `Error: Found advisory ${id} for module "${moduleName}"\n\tOverview: ${inline(
            overview
          )}\n\tRecommendation: ${inline(recommendation)}\n`
        );
      });
    }
  } catch (error) {
    process.exitCode = error.code || 1;
    console.error(error);
  }
});
