import { promises as fs } from "node:fs";
import path from "node:path";

export const EXAMPLE_FILE_SUFFIXES = [".example.tsx", ".example.ts"];

export const DEFAULT_IGNORED_DIRECTORIES = new Set(["dist", "node_modules", "vendors", "storybook"]);

export const toPosix = (input) => input.split(path.sep).join("/");

export const isExampleFile = (fileName) => EXAMPLE_FILE_SUFFIXES.some((suffix) => fileName.endsWith(suffix));

export const pathSegments = (filePath) => toPosix(filePath).split("/");

export const isIgnoredPath = (filePath, ignoredDirectories = DEFAULT_IGNORED_DIRECTORIES) =>
  pathSegments(filePath).some((segment) => ignoredDirectories.has(segment));

export const storyGroupName = (relativePath) => path.posix.dirname(relativePath).split("/").pop() ?? "Unknown";

export async function findExampleFiles(inputDir, options = {}) {
  const ignoredDirectories = options.ignoredDirectories ?? DEFAULT_IGNORED_DIRECTORIES;
  const files = [];

  async function walk(currentDir) {
    const entries = await fs.readdir(currentDir, { withFileTypes: true });

    for (const entry of entries) {
      if (entry.isDirectory() && ignoredDirectories.has(entry.name)) {
        continue;
      }

      const fullPath = path.join(currentDir, entry.name);
      if (entry.isDirectory()) {
        await walk(fullPath);
      } else if (isExampleFile(entry.name)) {
        files.push(toPosix(path.relative(inputDir, fullPath)));
      }
    }
  }

  await walk(inputDir);
  return files.sort((left, right) => left.localeCompare(right));
}
