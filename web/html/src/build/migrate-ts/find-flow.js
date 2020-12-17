const fs = require("fs");
const path = require("path");
const util = require("util");
const glob = util.promisify(require("glob"));
const exec = util.promisify(require("child_process").exec);

const args = require("./args");

/** Given a list of files & directories, find JS files with Flow annotations */
(async () => {
  const { inputs, isVerbose } = args.parse(process.argv);

  const cwd = process.cwd();
  const searchPaths = inputs.map(item => path.resolve(cwd, item));
  if (isVerbose) {
    console.log(`got inputs:\n${searchPaths.join("\n")}`);
  }

  const [files, directories] = searchPaths.reduce(
    ([files, directories], input) => {
      const isDirectory = fs.existsSync(input) && fs.lstatSync(input).isDirectory();
      if (isDirectory) {
        directories.push(input);
      } else {
        files.push(input);
      }
      return [files, directories];
    },
    [[], []]
  );

  // Look for input files in given directories
  function globIn(directory) {
    return new Promise(resolve =>
      glob(
        "**/*.js",
        {
          // See https://github.com/isaacs/node-glob#options
          cwd: directory,
          dot: false,
          strict: true,
          nodir: true,
          absolute: true,
          ignore: ["**/node_modules/**", "**/build/**"],
        },
        (error, result) => {
          if (error) {
            console.log(`failed to find inputs in ${directory}`);
            console.log(error);
            process.exit(1);
          }
          if (isVerbose) {
            console.log(`found ${result.length} candidates in ${directory}`);
          }
          resolve(result);
        }
      )
    );
  }

  for (const item of directories) {
    if (isVerbose) {
      console.log(`looking for candidates in ${item}`);
    }
    // Glob has a weird API interface
    const candidates = await globIn(item);
    files.push(...candidates);
  }

  // Filter out repeats, if any
  const uniqueFiles = files.filter((value, index) => files.indexOf(value) === index);
  if (isVerbose) {
    console.log(`found ${uniqueFiles.length} candidate inputs in total`);
  }

  // TODO: Obsolete
  // Find which files are Flow files
  const flowFiles = [];
  try {
    const { stdout } = await exec(`grep -l '@flow' ${uniqueFiles.join(" ")}`);
    const matches = stdout
      .split("\n")
      .map(item => (item || "").trim())
      .filter(Boolean);
    flowFiles.push(...matches);
  } catch (error) {
    // If grep exits with an errorcode, there's no matches
    if (isVerbose) {
      console.log("found no flow files");
    }
    process.exit(0);
  }
  if (isVerbose) {
    console.log(`found ${flowFiles.length} inputs which have flow annotations`);
  }

  // Output to console so we can pipe it further
  const formatted = flowFiles.join("\n");
  console.log(formatted);
})();
