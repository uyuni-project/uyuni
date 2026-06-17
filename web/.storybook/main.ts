/* eslint-disable no-console */
import type { StorybookConfig } from "@storybook/react-webpack5";
import { createRequire } from "module";
import path, { dirname } from "path";
import { fileURLToPath } from "url";

import {
  generateLegacyStories,
  watchLegacyStorySources,
} from "../html/src/build/storybook/generate-legacy-stories-lib.js";
import webpackAlias from "../html/src/build/webpack/alias.js";
import { scssProcessingLoaders } from "../html/src/build/webpack/scss-loaders.js";

const require = createRequire(import.meta.url);

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const web = path.resolve(__dirname, "..");
const webHtmlSrc = path.resolve(web, "html/src");
const legacyStoriesOutputDir = path.resolve(webHtmlSrc, "storybook/generated");

// Generate legacy .stories.tsx wrappers from .example.tsx sources before Storybook collects stories,
await generateLegacyStories({
  inputDir: webHtmlSrc,
  outputDir: legacyStoriesOutputDir,
  cleanOutput: false,
});

const scssLoaders = (styleLoaderOptions: Record<string, unknown> = {}) => [
  {
    loader: require.resolve("style-loader"),
    options: styleLoaderOptions,
  },
  ...scssProcessingLoaders(),
];

const config: StorybookConfig = {
  stories: ["../html/src/**/*.stories.@(ts|tsx)", "../html/src/storybook/generated/**/*.stories.@(ts|tsx)"],
  addons: ["@storybook/addon-docs"],
  framework: {
    name: "@storybook/react-webpack5",
    options: {},
  },
  webpackFinal: async (webpackConfig, { configType }) => {
    if (configType === "DEVELOPMENT") {
      watchLegacyStorySources({
        inputDir: webHtmlSrc,
        outputDir: legacyStoriesOutputDir,
        onGenerated: (result: { count: number }) =>
          console.log(`[storybook] regenerated ${result.count} legacy stories`),
        onError: (error: unknown) => console.error("[storybook] legacy story generation failed", error),
      });
    }

    webpackConfig.resolve = webpackConfig.resolve ?? {};
    webpackConfig.resolve.alias = {
      ...(webpackConfig.resolve.alias ?? {}),
      ...webpackAlias,
      // The Uyuni app provides jQuery globally via the JSP layout; Storybook needs the npm package instead.
      jquery: require.resolve("jquery"),
    };
    webpackConfig.resolve.extensions = Array.from(
      new Set([...(webpackConfig.resolve.extensions ?? []), ".ts", ".tsx", ".js", ".jsx", ".json"])
    );
    webpackConfig.resolve.symlinks = false;

    webpackConfig.module = webpackConfig.module ?? {};
    webpackConfig.module.rules = [
      ...(webpackConfig.module.rules ?? []),
      {
        test: /\.(ts|js)x?$/,
        exclude: /node_modules/,
        use: [
          {
            loader: require.resolve("babel-loader"),
            options: {
              configFile: path.resolve(webHtmlSrc, ".babelrc"),
            },
          },
        ],
      },
      {
        resourceQuery: /raw/,
        type: "asset/source",
      },
      {
        test: /\.po$/,
        type: "json",
        use: {
          loader: path.resolve(__dirname, "../html/src/build/webpack/loaders/po-loader.js"),
        },
      },
      {
        test: /\.(png|jpe?g|gif|svg)$/i,
        type: "asset/resource",
        generator: {
          filename: "img/[hash][ext][query]",
        },
      },
      {
        test: /\.(eot|ttf|woff|woff2)$/i,
        type: "asset/resource",
        generator: {
          filename: "fonts/[hash][ext][query]",
        },
      },
      {
        test: /\.scss$/,
        resourceQuery: /lazy/,
        use: scssLoaders({ injectType: "lazyStyleTag" }),
      },
      {
        test: /\.scss$/,
        resourceQuery: { not: [/lazy/] },
        use: scssLoaders(),
      },
    ];

    return webpackConfig;
  },
};

export default config;
