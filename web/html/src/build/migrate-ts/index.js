const fs = require("fs");
const path = require("path");
const util = require("util");
const exec = util.promisify(require("child_process").exec);

const args = require("./args");

(async () => {
  try {
    const { inputs: rawInputs, isVerbose } = args.parse(process.argv);

    /** Execute shell command and log the output if verbose, always log errors */
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
    const inputPaths = rawInputs.map((item) => path.resolve(cwd, item));
    const inputs = inputPaths.join(" ");
    if (isVerbose) {
      console.log(`got input paths:\n${inputPaths.join("\n")}`);
    }

    // Sanity check
    console.log("finding inputs");
    for (const item of inputPaths) {
      // If the file exists, all is good; if it throws, the program exits
      await fs.promises.access(item);
    }

    // Run an automatic tool that performs basic syntax transforms
    console.log("migrate flow");
    await execAndLog(`yarn flow-to-ts ${inputs}`);

    // Find which files we failed to migrate and rename them so other matchers can work with them
    console.log("finding orphans");
    const needsReview = [];
    for (const item of inputPaths) {
      try {
        // If the file exists, it needs to be renamed
        await fs.promises.access(item);
        // Blindly move to tsx, the content will need to be manually inspected anyway
        const newName = item.replace(/.jsx?$/, ".tsx");
        await fs.promises.rename(item, newName);
        needsReview.push(newName);
      } catch {
        // File was already migrated to a new name, do nothing
      }
    }
    if (needsReview.length) {
      console.log(`the following files need manual review:\n\t${needsReview.join("\n\t")}`);
    }

    // At this point, our input paths are no longer correct since they're js or jsx files, but previous ops output ts or tsx files
    console.log("finding outputs");
    const tsPaths = [];
    for (const item of inputPaths) {
      try {
        // Check if we have a tsx file with a matching name, this is the most likely scenario
        const tsxPath = item.replace(/.jsx?$/, ".tsx");
        await fs.promises.access(tsxPath);
        tsPaths.push(tsxPath);
      } catch {
        try {
          // If no tsx is found, check for ts
          const tsPath = item.replace(/.jsx?$/, ".ts");
          await fs.promises.access(tsPath);
          tsPaths.push(tsPath);
        } catch (error) {
          // If there's no match, give up on this one, but keep trying to work with the rest of the files
          console.error(`failed to find output for: ${item}`);
        }
      }
    }
    if (isVerbose) {
      console.log(`got ts inputs:\n${tsPaths.join("\n")}`);
      console.log(tsPaths.length === inputPaths.length ? "lengths match" : "lengths do not match");
    }
    const tsInputs = tsPaths.join(" ");

    /**
     * A collection of automatic fixes for some of the most prevalent issues across the whole codebase.
     * Most of these are semantic differences between Flow and TS, others are issues because TS is stricter with types than Flow is.
     */

    const tempExtension = ".bak";

    // In Flow, the widest possible type is `Object`, in TS the equivalent type is `any`
    // const foo: Object -> const foo: any
    console.log("migrate object to any");
    await execAndLog(`sed -i'${tempExtension}' -e 's/: Object\\([^\\.]\\)/: any\\1/g' ${tsInputs}`);

    // React.useState(undefined) -> React.useState<any>(undefined)
    console.log("migrate untyped use state");
    await execAndLog(
      `sed -i'${tempExtension}' -e 's/React.useState(undefined)/React.useState<any>(undefined)/' ${tsInputs}`
    );

    // TODO: Review this with https://github.com/typescript-cheatsheets/react
    // React.ReactNode -> JSX.Element
    // console.log("migrate React.ReactNode to JSX.Element");
    // await execAndLog(`sed -i'${tempExtension}' -e 's/=> React.ReactNode/=> JSX.Element/' ${tsInputs}`);
    // await execAndLog(`sed -i'${tempExtension}' -e 's/: React.ReactNode {/: JSX.Element {/' ${tsInputs}`);

    // Array<Object> -> Array<any>
    console.log("migrate object array to any array");
    await execAndLog(`sed -i'${tempExtension}' -e 's/Array<Object>/Array<any>/' ${tsInputs}`);

    // Hash<...> -> Record<...>
    console.log("migrate hash to record");
    await execAndLog(`sed -i'${tempExtension}' -e 's/Hash</Record</' ${tsInputs}`);

    // In strict TS, an empty untyped object is of type `{}` and can't have keys added to it
    // let foo = {}; -> let foo: any = {};
    console.log("migrate untyped object initializations");
    await execAndLog(
      `sed -i'${tempExtension}' -e 's/let \\([a-zA-Z0-9]*\\) = {\\s*};/let \\1: any = {};/' ${tsInputs}`
    );
    await execAndLog(
      `sed -i'${tempExtension}' -e 's/const \\([a-zA-Z0-9]*\\) = {\\s*};/const \\1: any = {};/' ${tsInputs}`
    );
    await execAndLog(
      `sed -i'${tempExtension}' -e 's/var \\([a-zA-Z0-9]*\\) = {\\s*};/var \\1: any = {};/' ${tsInputs}`
    );

    // In strict TS, an empty untyped array is of type `never[]` and you can't push to it without adding a type
    // let foo = []; -> let foo: any[] = [];
    console.log("migrate untyped array initializations");
    await execAndLog(
      `sed -i'${tempExtension}' -e 's/let \\([a-zA-Z0-9]*\\) = [\\s*];/let \\1: any[] = [];/' ${tsInputs}`
    );
    await execAndLog(
      `sed -i'${tempExtension}' -e 's/const \\([a-zA-Z0-9]*\\) = [\\s*];/const \\1: any[] = [];/' ${tsInputs}`
    );
    await execAndLog(
      `sed -i'${tempExtension}' -e 's/var \\([a-zA-Z0-9]*\\) = [\\s*];/var \\1: any[] = [];/' ${tsInputs}`
    );

    // TS doesn't know what the type of this is, but we do
    // jqXHR: any -> jqXHR: JQueryXHR
    console.log("annotate jqXHR types");
    await execAndLog(`sed -i'${tempExtension}' -e 's/jqXHR: any/jqXHR: JQueryXHR/' ${tsInputs}`);

    // There is no excuse to keep these around anymore
    // "use strict"; -> remove
    // /* eslint-disable */ -> remove
    console.log("remove deprecated annotations");
    await execAndLog(`sed -i'${tempExtension}' -e 's/"use strict";//' ${tsInputs}`);
    await execAndLog(`sed -i'${tempExtension}' -e 's/\\/\\* eslint-disable \\*\\///' ${tsInputs}`);

    // Find which imported files have type annotations but were not included in the migration
    console.log("finding untyped annotated imports");
    {
      const { stdout } = await execAndLog(
        `(yarn --silent tsc 2>&1 || true) | tee | grep "can only be used in TypeScript files" | sed -e 's/\\.js.*/.js/' | uniq`
      );
      const paths = stdout.split("\n").filter((item) => !!item.trim());
      if (paths.length) {
        console.log(
          `the following imported files have annotations but are not marked as typed:\n\t${paths.join("\n\t")}`
        );
        console.log(`to try and migrate them, run\n\tyarn migrate ${paths.join(" ")}`);
      }
    }

    // Standardize the formatting between all outputs
    console.log("formatting outputs");
    await execAndLog(`yarn prettier ${tsInputs}`);

    // Remove any temporary files
    console.log("cleaning up");
    for (const item of tsPaths) {
      const tempPath = item + tempExtension;
      try {
        // This path might not exist at all
        await fs.promises.access(tempPath);
        await fs.promises.unlink(tempPath);
        if (isVerbose) {
          console.log(`deleted backup file ${tempPath}`);
        }
      } catch {
        // Do nothing
      }
    }

    console.log("\ndone with automations, try running `yarn tsc` to find any remaining issues\n");
  } catch (error) {
    console.error((error && error.message) || error || "Unknown error");
    process.exit(1);
  }
})();
