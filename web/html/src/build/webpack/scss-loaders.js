import autoprefixer from "autoprefixer";
import { createRequire } from "node:module";
import path, { dirname } from "node:path";
import { fileURLToPath } from "node:url";

const require = createRequire(import.meta.url);

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const webHtmlSrc = path.resolve(__dirname, "../..");

// css-loader → postcss-loader → sass-loader chain shared between the app's webpack build
// and Storybook. Callers prepend their own injector (MiniCssExtractPlugin.loader for the
// app, style-loader for Storybook) and may override `localIdentName` for production hashes.
export function scssProcessingLoaders({ localIdentName = "[path][name]__[local]--[hash:base64:5]" } = {}) {
  return [
    {
      loader: require.resolve("css-loader"),
      options: {
        modules: {
          auto: true,
          localIdentName,
        },
      },
    },
    {
      loader: require.resolve("postcss-loader"),
      options: {
        postcssOptions: {
          plugins: [autoprefixer],
        },
      },
    },
    {
      loader: require.resolve("sass-loader"),
      options: {
        sassOptions: {
          loadPaths: [webHtmlSrc],
        },
      },
    },
  ];
}
