const path = require('path');
const {pages} = require("../manager/index");
const CopyWebpackPlugin = require('copy-webpack-plugin')
const CleanWebpackPlugin = require('clean-webpack-plugin');
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const TerserPlugin = require('terser-webpack-plugin')


module.exports = (env, argv) => {

  const  isProductionMode = argv && argv.mode !== "development";

  let pluginsInUse = [
    new CleanWebpackPlugin(['dist'], {  root: path.resolve(__dirname, "../")}),
    new CopyWebpackPlugin([{ from: path.resolve(__dirname, "../../javascript"), to: path.resolve(__dirname, "../dist/javascript") }]),
  ];

  if(isProductionMode) {
    pluginsInUse = [
      ...pluginsInUse,
      new LicenseCheckerWebpackPlugin({
        outputFilename: "../vendors/npm.licenses.structured.js",
        outputWriter: path.resolve(__dirname, "../vendors/licenses.template.ejs"),
      }),
      new LicenseCheckerWebpackPlugin({
        outputFilename: "../vendors/npm.licenses.txt",
      }),
    ]
  } else {
    pluginsInUse = [
      ...pluginsInUse,
      new CopyWebpackPlugin([{ from: path.resolve(__dirname, "../../../../branding/css"), to: path.resolve(__dirname, "../dist/css") }]),
    ]
  }


  return [{
    entry: pages,
    output: {
      filename: `[name].bundle.js`,
      path: path.resolve(__dirname, "../dist"),
      publicPath: '/'
    },
    optimization: {
      minimizer: [new TerserPlugin({extractComments: true})],
      splitChunks: {
        cacheGroups: {
          vendor: {
            test: /node_modules/,
            chunks: "all",
            name: "vendors/vendors",
            enforce: true
          }
        }
      }
    },
    module: {
      rules: [
        {
          test: /\.js$/,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader"
          }
        },
        {
          test: /\.css$/,
          use: [
            {loader: 'style-loader'},
            {
              loader: 'css-loader',
              options: {
                modules: true
              }
            }
          ]
        }
      ]
    },
    resolve: {
      alias: {
        components: path.resolve(__dirname, '../components/'),
        core: path.resolve(__dirname, '../core/'),
        utils: path.resolve(__dirname, '../utils/'),
      }
    },
    plugins: pluginsInUse,
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
