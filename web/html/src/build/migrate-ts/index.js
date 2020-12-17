const path = require("path");
const util = require("util");
const exec = util.promisify(require("child_process").exec);

const args = require("./args");

(async () => {
  try {
    const { inputs, isVerbose } = args.parse(process.argv);

    async function execAndLog(...args) {
      const result = await exec(...args);
      const { stdout, stderr } = result;
      if (isVerbose) {
        console.log(stdout);
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
    const inputPaths = inputs.map(item => path.resolve(cwd, item));
    if (isVerbose) {
      console.log(`got inputs:\n${inputPaths.join("\n")}`);
    }

    console.log("migrate flow");
    {
      const { stdout, stderr } = await execAndLog(`yarn flow-to-ts ${inputPaths.join(" ")}`);
    }

    console.log("untyped annotated files");
    {
      // This find untyped annotated files across the whole project, not only the inputs we have
      const { stdout, stderr } = await execAndLog(
        `(yarn tsc 2>&1 || true) | grep 'can only be used in TypeScript files' | sed -e 's/\.js.*/.js/' | uniq`
      );
      const paths = stdout
        .split("\n")
        .filter(item => !!item.trim())
        // Tsc gives out relative paths, make them absolute
        .map(item => path.resolve(cwd, item))
        // Trim the result to our input paths
        .filter(item => inputPaths.indexOf(item) !== -1);
      if (isVerbose) {
        console.log(`got untyped annotated\n:${paths.join("\n")}`);
      }
    }
  } catch (error) {
    console.error(error);
    process.exit(1);
  }
})();
