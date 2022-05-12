const { exec } = require("child_process");

const ignore = require("../.auditignore.js");

const inline = (message) => {
  return (message || "").replace(/\r?\n/g, " ");
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
    if (!advisories.length) {
      process.exitCode = 0;
      return;
    }

    const validAdvisories = advisories.filter((item) => {
      const { module_name: moduleName, id, overview, recommendation } = item.data.advisory;
      if (ignore[moduleName]) {
        console.info(
          `Warning: Ignoring advisory ${id} for module "${moduleName}"\n\tOverview: ${inline(
            overview
          )}\n\tRecommendation: ${inline(recommendation)}\n\tReason for ignoring: ${ignore[moduleName]}\n`
        );
        return false;
      }
      return true;
    });

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
      return;
    }

    process.exitCode = 0;
    return;
  } catch (error) {
    process.exitCode = error.code || 1;
    console.error(error);
    return;
  }
});
