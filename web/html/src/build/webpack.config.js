const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const webpackAlias = require("./webpack.alias");

const DEVSERVER_WEBSOCKET_PATHNAME = "/ws";

module.exports = (env, argv) => {
  const isProductionMode = argv && argv.mode !== "development";

  let pluginsInUse = [
    new CleanWebpackPlugin(["dist"], { root: path.resolve(__dirname, "../") }),
    new CopyWebpackPlugin({
      patterns: [
        { from: path.resolve(__dirname, "../../javascript"), to: path.resolve(__dirname, "../dist/javascript") },
      ],
    }),
    new CopyWebpackPlugin({
      patterns: [{ from: path.resolve(__dirname, "../../../po"), to: path.resolve(__dirname, "../dist/po") }],
    }),
  ];

  if (isProductionMode) {
    pluginsInUse = [
      ...pluginsInUse,
      new LicenseCheckerWebpackPlugin({
        // If we want, we could check licenses at build time via https://github.com/openSUSE/obs-service-format_spec_file or similar in the future
        // allow: [...],
        // emitError: true,
        outputFilename: "../vendors/npm.licenses.structured.js",
        outputWriter: path.resolve(__dirname, "../vendors/licenses.template.ejs"),
      }),
      new LicenseCheckerWebpackPlugin({
        outputFilename: "../vendors/npm.licenses.txt",
      }),
    ];
  } else {
    pluginsInUse = [
      ...pluginsInUse,
      new CopyWebpackPlugin({
        patterns: [
          { from: path.resolve(__dirname, "../../../../branding/css"), to: path.resolve(__dirname, "../dist/css") },
        ],
      }),
    ];
  }

  return [
    {
      entry: {
        "javascript/manager/main": "./manager/index.ts",
      },
      output: {
        filename: `[name].bundle.js`,
        path: path.resolve(__dirname, "../dist/"),
        chunkFilename: "javascript/manager/[name].bundle.js",
        publicPath: "/",
      },
      devtool: isProductionMode ? "source-map" : "eval-source-map",
      module: {
        rules: [
          {
            test: /\.(ts|js)x?$/,
            exclude: /node_modules/,
            use: {
              loader: "babel-loader",
            },
          },
          {
            test: /\.css$/,
            exclude: /node_modules/,
            use: [
              { loader: "style-loader" },
              {
                loader: "css-loader",
                options: {
                  modules: true,
                },
              },
            ],
          },
          {
            test: /\.css$/,
            include: /node_modules/,
            use: [{ loader: "style-loader" }, { loader: "css-loader" }],
          },
          {
            test: /\.po$/,
            type: "json",
            use: {
              loader: path.resolve(__dirname, "loaders/po-loader.js"),
            },
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
          // Don't write changes to disk so we can do hard reloads only on static file changes in the future
          writeToDisk: false,
          // Allow proxying requests to root "/" (disabled by default), see https://webpack.js.org/configuration/dev-server/#devserverproxy
          index: false,
        },
      },
    },
  ];
};
