import ReactRefreshWebpackPlugin from "@pmmmwh/react-refresh-webpack-plugin";
import autoprefixer from "autoprefixer";
import CleanWebpackPlugin from "clean-webpack-plugin";
import CopyWebpackPlugin from "copy-webpack-plugin";
import MiniCssExtractPlugin from "mini-css-extract-plugin";
import { createRequire } from "node:module";
import path, { dirname } from "node:path";
import { fileURLToPath } from "node:url";
import SpeedMeasurePlugin from "speed-measure-webpack-plugin";
import devServer from "./dev-server.js";

const require = createRequire(import.meta.url);

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const web = path.resolve(__dirname, "../../../..");
const webHtmlSrc = path.resolve(web, "./html/src");
const dist = path.resolve(webHtmlSrc, "./dist");

import GenerateStoriesPlugin from "./plugins/generate-stories-plugin.js";
import webpackAlias from "./alias.js";

export default (env, opts) => {
  let pluginsInUse = [];
  const isProductionMode = opts.mode === "production";
  const moduleName = isProductionMode ? "[id].[chunkhash]" : "[id]";

  if (opts.measurePerformance) {
    pluginsInUse.push(new SpeedMeasurePlugin());
  }

  if (!isProductionMode) {
    pluginsInUse.push(
      new ReactRefreshWebpackPlugin({
        overlay: false,
      })
    );
  }

  pluginsInUse = [
    ...pluginsInUse,
    new CleanWebpackPlugin(["dist"], { root: dist }),
    new CopyWebpackPlugin([
      // Legacy scripts
      { from: path.resolve(web, "./html/javascript"), to: path.resolve(dist, "./javascript") },
      // Translations
      { from: path.resolve(web, "./po"), to: path.resolve(dist, "./po") },
      // Unimported branding assets
      {
        from: path.resolve(webHtmlSrc, "./branding/fonts/font-spacewalk"),
        to: path.resolve(dist, "./fonts/font-spacewalk"),
      },
      // TODO: Copy all font licenses too
      { from: path.resolve(webHtmlSrc, "./branding/img"), to: path.resolve(dist, "./img") },
      // Any non-compiled CSS files will be compiled by their entry points
      {
        from: path.resolve(webHtmlSrc, "./branding/css/*.css"),
        context: path.resolve(webHtmlSrc, "./branding/css"),
        to: path.resolve(dist, "./css"),
      },
      {
        from: path.resolve(web, "./node_modules/bootstrap/dist/js/bootstrap.bundle.min.js"),
        to: path.resolve(dist, "./javascript/legacy/bootstrap-webpack.js"),
      },
      {
        from: path.resolve(web, "./node_modules/jquery/dist/jquery.min.js"),
        to: path.resolve(dist, "./javascript/legacy"),
      },
      {
        from: path.resolve(web, "./node_modules/jquery-ui/dist/jquery-ui.js"),
        to: path.resolve(dist, "./javascript/legacy"),
      },
      // TODO: In the future it would be nice to bundle this instead of copying it
      {
        from: path.resolve(web, "./node_modules/font-awesome"),
        to: path.resolve(dist, "./fonts/font-awesome"),
      },
      {
        from: path.resolve(web, "./node_modules/pwstrength-bootstrap/dist/pwstrength-bootstrap-1.0.2.js"),
        to: path.resolve(dist, "./javascript/legacy"),
      },
      // TODO: Take only what we need after we've confirmed it works fine, otherwise there's a lot of fluff in this
      {
        from: path.resolve(web, "./node_modules/ace-builds/src-min-noconflict"),
        to: path.resolve(dist, "./javascript/legacy/ace-editor"),
      },
    ]),
    new MiniCssExtractPlugin({
      chunkFilename: `css/${moduleName}.css`,
    }),
    new GenerateStoriesPlugin({
      inputDir: webHtmlSrc,
      outputFile: path.resolve(webHtmlSrc, "./manager/storybook/stories.generated.ts"),
    }),
  ];

  if (opts.verbose) {
    console.log("pluginsInUse:");
    console.log(pluginsInUse);
    console.log("webpack mode: " + opts.mode);
  }

  const config = {
    mode: opts.mode,
    entry: {
      "javascript/manager/main": path.resolve(webHtmlSrc, "./manager/index.ts"),
      "css/updated-suse-light": path.resolve(webHtmlSrc, "./branding/css/suse-light.scss"),
      "css/updated-suse-dark": path.resolve(webHtmlSrc, "./branding/css/suse-dark.scss"),
      "css/updated-uyuni": path.resolve(webHtmlSrc, "./branding/css/uyuni.scss"),
    },
    output: {
      // This needs to be constant as it's referenced from layout_head.jsp etc. All uses need to specify cache bust where imported.
      filename: `[name].js`,
      path: dist,
      chunkFilename: `javascript/manager/${moduleName}.js`,
      publicPath: "/",
    },
    optimization: {
      chunkIds: "named",
      moduleIds: "named",
    },
    node: {
      __filename: true,
      __dirname: true,
    },
    devtool: isProductionMode ? "source-map" : "eval-source-map",
    module: {
      rules: [
        {
          oneOf: [
            {
              resourceQuery: /raw/,
              type: "asset/source",
            },
            {
              test: /\.(ts|js)x?$/,
              exclude: /node_modules/,
              use: [
                {
                  loader: require.resolve("babel-loader"),
                  options: {
                    configFile: path.resolve(webHtmlSrc, "./.babelrc"),
                    plugins: isProductionMode ? undefined : [require.resolve("react-refresh/babel")],
                  },
                },
              ],
            },
          ],
        },
        {
          // Stylesheets that are imported directly by components
          test: /(components|core|manager)\/.*\.css$/,
          exclude: /node_modules/,
          use: [
            MiniCssExtractPlugin.loader,
            {
              loader: require.resolve("css-loader"),
              options: {
                modules: true,
              },
            },
          ],
        },
        {
          // Stylesheets of third party dependencies
          test: /\.css$/,
          include: /node_modules/,
          use: [{ loader: require.resolve("style-loader") }, { loader: require.resolve("css-loader") }],
        },
        {
          test: /\.po$/,
          type: "json",
          use: {
            loader: path.resolve(__dirname, "./loaders/po-loader.js"),
          },
        },
        {
          // Assets that are imported directly by components
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
        // See https://getbootstrap.com/docs/5.3/getting-started/webpack/
        {
          test: /\.(scss)$/,
          use: [
            MiniCssExtractPlugin.loader,
            // {
            //   // Adds CSS to the DOM by injecting a `<style>` tag
            //   loader: "style-loader",
            // },
            {
              // Interprets `@import` and `url()` like `import/require()` and will resolve them
              loader: require.resolve("css-loader"),
              options: {
                modules: {
                  auto: true,
                  localIdentName: isProductionMode
                    ? "[hash:base64:5]" // This is the default value for CSS modules
                    : "[path][name]__[local]--[hash:base64:5]",
                },
              },
            },
            {
              // Loader for webpack to process CSS with PostCSS
              loader: require.resolve("postcss-loader"),
              options: {
                postcssOptions: {
                  plugins: [autoprefixer],
                },
              },
            },
            {
              // Loads a SASS/SCSS file and compiles it to CSS
              loader: require.resolve("sass-loader"),
              options: {
                sassOptions: {
                  loadPaths: [webHtmlSrc],
                },
              },
            },
          ],
        },
      ],
    },
    resolve: {
      alias: webpackAlias,
      extensions: [".ts", ".tsx", ".js", ".jsx", ".json"],
      symlinks: false,
    },
    plugins: pluginsInUse,
    devServer: devServer(env),
  };

  if (opts.force) {
    config.cache = false;
  }

  return config;
};
