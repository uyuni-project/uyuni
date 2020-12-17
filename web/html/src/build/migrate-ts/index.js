const fs = require("fs");
const path = require("path");
const util = require("util");
const exec = util.promisify(require("child_process").exec);

const args = require("./args");

(async () => {
  try {
    const { inputs: rawInputs, isVerbose } = args.parse(process.argv);

    async function execAndLog(...args) {
      const result = await exec(...args);
      const { stdout, stderr } = result;
      if (isVerbose) {
        if ((stdout || "").trim()) {
          console.log(stdout);
        } else {
          console.log("(no output)");
        }
      }
      if (stderr) {
        console.error("step had errors:");
        console.error(stderr);
        console.log("trying to proceed anyway");
      }
      return result;
    }

    const cwd = process.cwd();
    // Make all paths absolute to avoid any issues
    const inputPaths = rawInputs.map(item => path.resolve(cwd, item));
    const inputs = inputPaths.join(" ");
    if (isVerbose) {
      console.log(`got inputs:\n${inputPaths.join("\n")}`);
    }

    console.log("migrate flow");
    await execAndLog(`yarn flow-to-ts ${inputs}`);

    // Find which files we failed to migrate and rename them so other toolsets can work with them
    console.log("finding orphans");
    for (const item of inputPaths) {
      try {
        // If the file exists, it needs to be renamed
        await fs.promises.access(item);
        // Blindly move to tsx, the content will need to be manually inspected anyway
        const newName = item.replace(/.jsx?$/, ".tsx");
        await fs.promises.rename(item, newName);
        console.warn(`needs manual review: ${newName}`);
      } catch {
        // File was already migrated to a new name, do nothing
      }
    }

    // At this point, our input paths are no longer correct since they're js or jsx files, but we have output ts or tsx files
    const tsPaths = [];
    console.log("finding outputs");
    for (const item of inputPaths) {
      try {
        // This is the most likely output
        const tsxPath = item.replace(/.jsx?$/, ".tsx");
        await fs.promises.access(tsxPath);
        tsPaths.push(tsxPath);
      } catch {
        try {
          const tsPath = item.replace(/.jsx?$/, ".ts");
          await fs.promises.access(tsPath);
          tsPaths.push(tsPath);
        } catch (error) {
          console.error(`failed to find output for: ${item}`);
        }
      }
    }
    if (isVerbose) {
      console.log(`got ts inputs:\n${tsPaths.join("\n")}`);
      console.log(tsPaths.length === inputPaths.length ? "lengths match" : "lengths do not match");
    }
    const tsInputs = tsPaths.join(" ");

    console.log("migrate object to any");

    await execAndLog(`sed -i '' -e 's/: Object\\([^\\.]\\)/: any\\1/g' ${tsInputs}`);

    console.log("migrate untyped use state");
    await execAndLog(`sed -i '' -e 's/React.useState(undefined)/React.useState<any>(undefined)/' ${tsInputs}`);

    console.log("migrate React.ReactNode to JSX.Element");
    await execAndLog(`sed -i '' -e 's/=> React.ReactNode/=> JSX.Element/' ${tsInputs}`);
    await execAndLog(`sed -i '' -e 's/: React.ReactNode {/: JSX.Element {/' ${tsInputs}`);

    console.log("migrate object array to any array");
    await execAndLog(`sed -i '' -e 's/Array<Object>/Array<any>/' ${tsInputs}`);

    console.log('\ndone with automations, try running `yarn tsc` to find any remaining issues\n');
  } catch (error) {
    console.error(error);
    process.exit(1);
  }
})();
