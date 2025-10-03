const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const webpackAlias = require("./webpack.alias");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const SpeedMeasurePlugin = require("speed-measure-webpack-plugin");
const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");

const autoprefixer = require("autoprefixer");

const GenerateStoriesPlugin = require("./plugins/generate-stories-plugin");

const DEVSERVER_WEBSOCKET_PATHNAME = "/ws";

module.exports = (env, opts) => {
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
    new CleanWebpackPlugin(["dist"], { root: path.resolve(__dirname, "../") }),
    new CopyWebpackPlugin([
      // Legacy scripts
      { from: path.resolve(__dirname, "../../javascript"), to: path.resolve(__dirname, "../dist/javascript") },
      // Translations
      { from: path.resolve(__dirname, "../../../po"), to: path.resolve(__dirname, "../dist/po") },
      // Unimported branding assets
      {
        from: path.resolve(__dirname, "../branding/fonts/font-spacewalk"),
        to: path.resolve(__dirname, "../dist/fonts/font-spacewalk"),
      },
      // TODO: Copy all font licenses too
      { from: path.resolve(__dirname, "../branding/img"), to: path.resolve(__dirname, "../dist/img") },
      // Any non-compiled CSS files will be compiled by their entry points
      {
        from: path.resolve(__dirname, "../branding/css/*.css"),
        context: path.resolve(__dirname, "../branding/css"),
        to: path.resolve(__dirname, "../dist/css"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/bootstrap/dist/js/bootstrap.bundle.min.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy/bootstrap-webpack.js"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/jquery/dist/jquery.min.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/jquery-ui/dist/jquery-ui.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      // TODO: In the future it would be nice to bundle this instead of copying it
      {
        from: path.resolve(__dirname, "../node_modules/font-awesome"),
        to: path.resolve(__dirname, "../dist/fonts/font-awesome"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/pwstrength-bootstrap/dist/pwstrength-bootstrap-1.0.2.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      // TODO: Take only what we need after we've confirmed it works fine, otherwise there's a lot of fluff in this
      {
        from: path.resolve(__dirname, "../node_modules/ace-builds/src-min-noconflict"),
        to: path.resolve(__dirname, "../dist/javascript/legacy/ace-editor"),
      },
    ]),
    new MiniCssExtractPlugin({
      chunkFilename: `css/${moduleName}.css`,
    }),
    new GenerateStoriesPlugin({
      inputDir: path.resolve(__dirname, "../manager"),
      outputFile: path.resolve(__dirname, "../manager/storybook/stories.generated.ts"),
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
      "javascript/manager/main": path.resolve(__dirname, "../manager/index.ts"),
      "css/updated-suse-light": path.resolve(__dirname, "../branding/css/suse-light.scss"),
      "css/updated-suse-dark": path.resolve(__dirname, "../branding/css/suse-dark.scss"),
      "css/updated-uyuni": path.resolve(__dirname, "../branding/css/uyuni.scss"),
    },
    output: {
      // This needs to be constant as it's referenced from layout_head.jsp etc. All uses need to specify cache bust where imported.
      filename: `[name].js`,
      path: path.resolve(__dirname, "../dist/"),
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
                    configFile: path.resolve(__dirname, "../.babelrc"),
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
            loader: path.resolve(__dirname, "loaders/po-loader.js"),
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
                  loadPaths: [path.resolve(__dirname, "../")],
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
    devServer: {
      hot: true,
      open: true,
      static: {
        directory: path.resolve(__dirname, "../dist"),
        publicPath: "/",
        // This is currently redundant, but will become relevant when we include static files in Webpack
        watch: true,
      },
      server: {
        type: "https",
      },
      client: {
        webSocketURL: {
          // Hardcode this so it always matches
          pathname: DEVSERVER_WEBSOCKET_PATHNAME,
        },
        logging: "error",
      },
      // Override CORS headers for `yarn storybook`, these are not required otherwise
      headers: {
        "Access-Control-Allow-Origin": "*",
      },
      /**
       * The documentation isn't very good for this, but shortly we're proxying everything besides what comes out of Webpack through to the provided server
       * See https://webpack.js.org/configuration/dev-server/#devserverproxy and https://github.com/chimurai/http-proxy-middleware#options
       */
      proxy: [
        {
          target: env && env.server,
          // Proxy everything, including websockets, besides the Webpack updates websocket
          context: ["!" + DEVSERVER_WEBSOCKET_PATHNAME],
          ws: true,
          /**
           * Rewrite the host and port on redirects, so we stay on the proxy after logging in, logging out etc
           * See https://github.com/http-party/node-http-proxy/issues/1227
           */
          autoRewrite: true,
          changeOrigin: true,
          // Ignore sertificate errors for dev servers
          secure: false,
        },
      ],
      devMiddleware: {
        publicPath: "/",
        // If we ever integrate theme loading locally as opposeds to a global load in jsp, we can set this to false to get faster HMR
        writeToDisk: true,
        // Allow proxying requests to root "/" (disabled by default), see https://webpack.js.org/configuration/dev-server/#devserverproxy
        index: false,
      },
    },
  };

  if (opts.force) {
    config.cache = false;
  }

  return config;
};
