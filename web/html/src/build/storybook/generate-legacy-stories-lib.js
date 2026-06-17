import { promises as fs, watch } from "node:fs";
import path from "node:path";

import {
  DEFAULT_IGNORED_DIRECTORIES,
  findExampleFiles,
  isExampleFile,
  isIgnoredPath,
  storyGroupName,
  toPosix,
} from "./example-discovery.js";
import { storyTemplate } from "./story-template.js";

export { findExampleFiles, isIgnoredPath, storyGroupName, toPosix };
export { storyExportName, storyTemplate } from "./story-template.js";

const DEFAULT_INPUT_DIR = path.resolve("html/src");
const DEFAULT_OUTPUT_DIR = path.resolve(DEFAULT_INPUT_DIR, "storybook/generated");

const isGeneratedStoryFile = (fileName) => fileName.endsWith(".stories.tsx");

export const shouldRegenerateOnChange = (filePath, options = {}) => {
  if (!filePath) {
    return true;
  }

  const ignoredDirectories = options.ignoredDirectories ?? DEFAULT_IGNORED_DIRECTORIES;
  return !isIgnoredPath(filePath, ignoredDirectories) && isExampleFile(filePath);
};

async function findGeneratedStoryFiles(outputDir) {
  const files = [];

  async function walk(currentDir) {
    let entries;
    try {
      entries = await fs.readdir(currentDir, { withFileTypes: true });
    } catch (error) {
      if (error.code === "ENOENT") {
        return;
      }

      throw error;
    }

    for (const entry of entries) {
      const fullPath = path.join(currentDir, entry.name);
      if (entry.isDirectory()) {
        await walk(fullPath);
      } else if (isGeneratedStoryFile(entry.name)) {
        files.push(toPosix(path.relative(outputDir, fullPath)));
      }
    }
  }

  await walk(outputDir);
  return files.sort((left, right) => left.localeCompare(right));
}

export async function generateLegacyStories(options = {}) {
  const inputDir = options.inputDir ?? DEFAULT_INPUT_DIR;
  const outputDir = options.outputDir ?? DEFAULT_OUTPUT_DIR;
  const cleanOutput = options.cleanOutput ?? true;
  const exampleFiles = await findExampleFiles(inputDir, options);
  const generatedFiles = new Set(exampleFiles.map((filePath) => filePath.replace(/\.(tsx|ts)$/, ".stories.tsx")));

  if (cleanOutput) {
    await fs.rm(outputDir, { recursive: true, force: true });
  } else {
    await fs.mkdir(outputDir, { recursive: true });

    for (const staleFile of await findGeneratedStoryFiles(outputDir)) {
      if (!generatedFiles.has(staleFile)) {
        await fs.rm(path.join(outputDir, ...staleFile.split("/")), { force: true });
      }
    }
  }

  for (const relativePath of exampleFiles) {
    const outputPath = path.join(outputDir, ...relativePath.replace(/\.(tsx|ts)$/, ".stories.tsx").split("/"));
    await fs.mkdir(path.dirname(outputPath), { recursive: true });
    await fs.writeFile(outputPath, storyTemplate(relativePath), "utf8");
  }

  return {
    count: exampleFiles.length,
    exampleFiles,
    outputDir,
  };
}

export function watchLegacyStorySources(options = {}) {
  const inputDir = options.inputDir ?? DEFAULT_INPUT_DIR;
  const debounceMs = options.debounceMs ?? 100;
  const onGenerated = options.onGenerated ?? (() => undefined);
  const onError = options.onError ?? (() => undefined);
  let timeout;
  let isGenerating = false;
  let shouldRunAgain = false;

  const run = async () => {
    if (isGenerating) {
      shouldRunAgain = true;
      return;
    }

    isGenerating = true;
    try {
      const result = await generateLegacyStories({ ...options, cleanOutput: options.cleanOutput ?? false });
      onGenerated(result);
    } catch (error) {
      onError(error);
    } finally {
      isGenerating = false;
      if (shouldRunAgain) {
        shouldRunAgain = false;
        schedule();
      }
    }
  };

  const schedule = () => {
    clearTimeout(timeout);
    timeout = setTimeout(run, debounceMs);
  };

  const watcher = watch(inputDir, { recursive: true }, (_eventType, fileName) => {
    if (shouldRegenerateOnChange(fileName, options)) {
      schedule();
    }
  });

  return {
    close() {
      clearTimeout(timeout);
      watcher.close();
    },
  };
}
