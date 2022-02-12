// Parse CLI input args
const yargs = require("yargs/yargs");
const { hideBin } = require("yargs/helpers");

module.exports = {
  /** Parse input args */
  parse(args) {
    const parser = yargs(hideBin(args))
      .version(false)
      .option("v", {
        alias: "verbose",
        type: "boolean",
        default: false,
        describe: "Enable verbose logging",
      })
      .help("h")
      .alias("h", "help");

    const argv = parser.argv;
    const isVerbose = !!argv.verbose;
    // Everything after specified args
    const inputs = argv._;

    if (!inputs.filter(Boolean).length) {
      console.log("received no inputs");
      process.exit(1);
    }

    if (isVerbose) {
      console.log("verbose logging enabled");
    }

    return {
      isVerbose,
      inputs,
    };
  },
};
