const fs = require("fs").promises;
const path = require("path");

/** Automatically gather all imports for story files */
module.exports = class GenerateStoriesPlugin {
  didApply = false;
  outputFile = undefined;

  constructor({ outputFile }) {
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
   * @param {import("webpack").Compiler} compiler
   * @param {Function} callback
   */
  async beforeOrWatchRun(compiler, callback) {
    if (this.didApply) {
      callback();
      return;
    }
    this.didApply = true;

    /** Source directory for the compilation, an absolute path to `/web/html/src` */
    const webHtmlSrc = compiler.context;
    if (!this.outputFile.startsWith(webHtmlSrc)) {
      throw new RangeError("GenerateStoriesPlugin: `outputFile` is outside of the source code directory");
    }

    const files = await fs.readdir(webHtmlSrc, { recursive: true });
    const storyFilePaths = files
      .filter(
        (item) => !item.startsWith("node_modules") && (item.endsWith(".stories.ts") || item.endsWith(".stories.tsx"))
      )
      .sort();

    const stories = storyFilePaths.map((filePath) => {
      const safeName = this.wordify(filePath);
      // We use the parent directory name as the group name
      const groupName = path.dirname(filePath).split("/").pop() ?? "Unknown";
      return storyTemplate(filePath, safeName, groupName);
    });

    const output = fileTemplate(stories.join(""));
    await fs.writeFile(this.outputFile, output, "utf-8");
    console.log(`GenerateStoriesPlugin: wrote ${storyFilePaths.length} stories to ${this.outputFile}`);
    callback();
  }

  wordify(input) {
    return input.replace(/[\W_]+/g, "_");
  }
};

const storyTemplate = (filePath, safeName, groupName) =>
  `
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
 * See: web/html/src/build/plugins/generate-stories-plugin.js
 */

/* eslint-disable */
${content}
`.trim();
