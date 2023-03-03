const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const webpackAlias = require("./webpack.alias");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const SpeedMeasurePlugin = require("speed-measure-webpack-plugin");

const DEVSERVER_WEBSOCKET_PATHNAME = "/ws";

module.exports = (env, argv) => {
  const isProductionMode = argv && argv.mode !== "development";
  const measurePerformance = env.MEASURE_PERFORMANCE === "true";

  let pluginsInUse = [];

  if (measurePerformance) {
    pluginsInUse.push(new SpeedMeasurePlugin());
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
      { from: path.resolve(__dirname, "../branding/fonts"), to: path.resolve(__dirname, "../dist/fonts") },
      { from: path.resolve(__dirname, "../branding/img"), to: path.resolve(__dirname, "../dist/img") },
      // Any non-compiled CSS, Less files will be compiled by their entry points
      {
        from: path.resolve(__dirname, "../branding/css/*.css"),
        context: path.resolve(__dirname, "../branding/css"),
        to: path.resolve(__dirname, "../dist/css"),
      },
      /**
       * Scripts and dependencies we're migrating from susemanager-frontend-libs to spacewalk-web
       */
      {
        from: path.resolve(__dirname, "../node_modules/bootstrap/dist/js/bootstrap.min.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/jquery/dist/jquery.min.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/jquery-ui/jquery-ui.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      // Font-awesome
      {
        from: path.resolve(__dirname, "../node_modules/font-awesome/fonts"),
        to: path.resolve(__dirname, "../dist/fonts/font-awesome"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/font-awesome/css"),
        to: path.resolve(__dirname, "../dist/fonts/font-awesome"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/pwstrength-bootstrap/dist/pwstrength-bootstrap-1.0.2.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      // TODO: Take only what we need after we've confirmed it works fine, otherwise there's a lot of fluff in this
      {
        from: path.resolve(__dirname, "../node_modules/select2"),
        to: path.resolve(__dirname, "../dist/javascript/legacy/select2"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/select2-bootstrap-css/select2-bootstrap.css"),
        to: path.resolve(__dirname, "../dist/javascript/legacy/select2"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/timepicker/jquery.timepicker.js"),
        to: path.resolve(__dirname, "../dist/javascript/legacy"),
      },
      {
        from: path.resolve(__dirname, "../node_modules/timepicker/jquery.timepicker.css"),
        to: path.resolve(__dirname, "../dist/css/legacy"),
      },
      // TODO: Take only what we need after we've confirmed it works fine, otherwise there's a lot of fluff in this
      {
        from: path.resolve(__dirname, "../node_modules/ace-builds/src-min-noconflict"),
        to: path.resolve(__dirname, "../dist/javascript/legacy/ace-editor"),
      },
    ]),
    new MiniCssExtractPlugin({
      chunkFilename: "css/[name].css",
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
  }

  return {
    entry: {
      "javascript/manager/main": "./manager/index.ts",
      "css/uyuni": path.resolve(__dirname, "../branding/css/uyuni.less"),
      "css/susemanager-fullscreen": path.resolve(__dirname, "../branding/css/susemanager-fullscreen.less"),
      "css/susemanager-light": path.resolve(__dirname, "../branding/css/susemanager-light.less"),
      "css/susemanager-dark": path.resolve(__dirname, "../branding/css/susemanager-dark.less"),
    },
    output: {
      filename: `[name].bundle.js`,
      path: path.resolve(__dirname, "../dist/"),
      chunkFilename: "javascript/manager/[name].bundle.js",
      publicPath: "/",
      hashFunction: "md5",
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
          // Stylesheets that are imported directly by components
          test: /(components|core|manager)\/.*\.(css|less)$/,
          exclude: /node_modules/,
          use: [
            MiniCssExtractPlugin.loader,
            {
              loader: "css-loader",
              options: {
                modules: true,
              },
            },
            { loader: "less-loader" },
          ],
        },
        {
          // Global stylesheets
          test: /branding\/.*\.less$/,
          exclude: /node_modules/,
          use: [
            MiniCssExtractPlugin.loader,
            {
              loader: "css-loader",
              options: {
                // NB! This is crucial, we don't consume Bootstrap etc as a module, but as a regular style
                modules: false,
              },
            },
            { loader: "less-loader" },
          ],
        },
        {
          // Stylesheets of third party dependencies
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
        // Don't write changes to disk so we can do hard reloads only on static file changes in the future
        // TODO: Revert
        // writeToDisk: false,
        writeToDisk: true,
        // Allow proxying requests to root "/" (disabled by default), see https://webpack.js.org/configuration/dev-server/#devserverproxy
        index: false,
      },
    },
  };
};
