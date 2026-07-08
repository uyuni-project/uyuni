import { promises as fs } from "node:fs";
import path from "node:path";

import { findExampleFiles, storyGroupName } from "../../storybook/example-discovery.js";

/** Automatically gather all imports for story files */
export default class GenerateStoriesPlugin {
  didApply = false;
  inputDir = undefined;
  outputFile = undefined;

  constructor({ inputDir, outputFile }) {
    if (!inputDir) {
      throw new Error("GenerateStoriesPlugin: `inputDir` is not set");
    }
    this.inputDir = inputDir;

    if (!outputFile) {
      throw new Error("GenerateStoriesPlugin: `outputFile` is not set");
    }
    this.outputFile = outputFile;
  }

  /**
   * @param {import("webpack").Compiler} compiler
   */
  apply(compiler) {
    // See https://webpack.js.org/api/compiler-hooks/#hooks
    compiler.hooks.watchRun.tapAsync("GenerateStoriesPlugin", async (params, callback) =>
      this.beforeOrWatchRun(params, callback)
    );
    compiler.hooks.beforeRun.tapAsync("GenerateStoriesPlugin", async (params, callback) =>
      this.beforeOrWatchRun(params, callback)
    );
  }

  /**
   *
   * @param {import("webpack").Compiler} _compiler
   * @param {Function} callback
   */
  async beforeOrWatchRun(_compiler, callback) {
    if (this.didApply) {
      callback();
      return;
    }
    this.didApply = true;

    const storyFilePaths = await findExampleFiles(this.inputDir);

    const stories = storyFilePaths.map((filePath) => {
      const safeName = this.wordify(filePath);
      return storyTemplate(filePath, safeName, storyGroupName(filePath));
    });

    const output = fileTemplate(stories.join(""));
    await fs.writeFile(this.outputFile, output, "utf-8");
    console.log(`GenerateStoriesPlugin: wrote ${storyFilePaths.length} stories to ${this.outputFile}`);
    callback();
  }

  wordify(input) {
    return input.replaceAll(/[\W_]+/g, "_");
  }
}

const storyTemplate = (filePath, safeName, groupName) =>
  `
// @ts-ignore
import ${safeName}_component from "${filePath}";
import ${safeName}_raw from "${filePath}?raw";

export const ${safeName} = {
  path: "${filePath}",
  title: "${path.basename(filePath)}",
  groupName: "${groupName}",
  component: ${safeName}_component,
  raw: ${safeName}_raw,
};
`;

const fileTemplate = (content) =>
  `
/**
 * NB! This is a generated file!
 * Any changes you make here will be lost.
 * See: web/html/src/build/webpack/plugins/generate-stories-plugin.js
 */

/* eslint-disable */
${content}
`.trim();
