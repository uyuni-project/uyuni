import { generateLegacyStories, toPosix, watchLegacyStorySources } from "./generate-legacy-stories-lib.js";

const logGenerated = (result) => {
  console.log(`Generated ${result.count} legacy Storybook stories in ${toPosix(result.outputDir)}`);
};

const run = async () => {
  const watchMode = process.argv.includes("--watch");

  logGenerated(await generateLegacyStories({ cleanOutput: !watchMode }));

  if (watchMode) {
    console.log("Watching legacy .example.ts(x) files for Storybook wrapper updates...");
    watchLegacyStorySources({
      onGenerated: logGenerated,
      onError: (error) => console.error(error),
    });
  }
};

run().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
