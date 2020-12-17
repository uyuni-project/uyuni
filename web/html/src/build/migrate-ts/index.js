const path = require("path");
const util = require("util");
const exec = util.promisify(require("child_process").exec);

const args = require("./args");

(async () => {
  const { inputs, isVerbose } = args.parse(process.argv);

  const cwd = process.cwd();
  // Make all paths absolute to avoid any issues
  const inputPaths = inputs.map(item => path.resolve(cwd, item));
  if (isVerbose) {
    console.log(`got inputs:\n${inputPaths.join("\n")}`);
  }

  console.log("migrate flow");
  try {
    const output = await exec(`yarn flow-to-ts ${inputPaths}`);
    console.log(output);
  } catch (error) {
    console.error(error);
    process.exit(1);
  }
})();
