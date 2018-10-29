const path = require('path');
const {pages} = require("../manager/index");
const webpack = require("webpack");
const CopyWebpackPlugin = require('copy-webpack-plugin')
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = (env, argv) => {

  const  isProductionMode = argv && argv.mode !== "development";

  let optimization = {};
  if(isProductionMode) {
    // If we are on production mode we want to make sure we don't mix vendors code with source code
    optimization = {
      splitChunks: {
        cacheGroups: {
          vendor: {
            test: /node_modules/,
            chunks: "all",
            name: "extravendors.notdeclared",
            priority: 10,
            enforce: true
          }
        }
      }
    }
  }

  return [{
    entry: pages,
    output: {
      filename: `[name].bundle.js`,
      path: path.resolve(__dirname, "../dist"),
      publicPath: '/'
    },
    optimization,
    module: {
      rules: [
        {
          test: /\.js$/,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader"
          }
        }
      ]
    },
    resolve: {
      alias: {
        components: path.resolve(__dirname, '../components/'),
        utils: path.resolve(__dirname, '../utils/'),
      }
    },
    plugins: [
      new CleanWebpackPlugin(['extravendors.notdeclared.bundle.js', "javascript"], {  root: path.resolve(__dirname)}),
      new webpack.DllReferencePlugin({
        manifest: path.resolve(__dirname, "../dist/vendors/vendors-manifest.json"),
      }),
      new CopyWebpackPlugin([{ from: path.resolve(__dirname, "../../javascript"), to: path.resolve(__dirname, "../dist/javascript") }])
    ],
    devServer: {
      contentBase: path.resolve(__dirname, "../dist"),
      publicPath: "/",
      progress: true,
      https: true,
      open: true,
      writeToDisk: argv && argv.writeToDisk,
      proxy: [{
        context: ['!/sockjs-node/**'],
        target: (argv && argv.server) || "https://suma-refhead-srv.mgr.suse.de",
        ws: true,
        secure: false,
        // logLevel: "debug",
        bypass: function(req, res, proxyOptions) {
          //We need this little trick to serve the local vendors.bundles.js, otherwise it would have been proxied as it isn't indexed by this webpack configuration
          if (req.url.indexOf('/vendors/vendors.bundle.js') !== -1) {
            return '/vendors/vendors.bundle.js';
          }
        }
      }]
    },
  }]
};
