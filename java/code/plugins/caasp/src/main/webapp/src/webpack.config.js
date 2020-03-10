const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin')
const CleanWebpackPlugin = require('clean-webpack-plugin');
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const TerserPlugin = require('terser-webpack-plugin')
const webpackAlias = require('./webpack.alias');

module.exports = (env, argv) => {

  const  isProductionMode = argv && argv.mode !== "development";

  let pluginsInUse = [
    new CleanWebpackPlugin(['dist'], {  root: path.resolve(__dirname, "../")}),
    new CopyWebpackPlugin([{ from: path.resolve(__dirname, "../../javascript"), to: path.resolve(__dirname, "../dist/javascript") }]),
  ];

//  if(isProductionMode) {
//    pluginsInUse = [
//      ...pluginsInUse,
//      new LicenseCheckerWebpackPlugin({
//        outputFilename: "../vendors/npm.licenses.structured.js",
//        outputWriter: path.resolve(__dirname, "../vendors/licenses.template.ejs"),
//      }),
//      new LicenseCheckerWebpackPlugin({
//        outputFilename: "../vendors/npm.licenses.txt",
//      }),
//    ]
//  } else {
//    pluginsInUse = [
//      ...pluginsInUse,
//      new CopyWebpackPlugin([{ from: path.resolve(__dirname, "../../../../branding/css"), to: path.resolve(__dirname, "../dist/css") }]),
//    ]
//  }

  return [{
    entry: {
      'javascript/manager/plugins/caasp': './manager/index.js'
    },
    output: {
      filename: `[name].bundle.js`,
      path: path.resolve(__dirname, "../target/" ),
      chunkFilename: 'javascript/manager/plugins/caasp/[name].bundle.js',
      publicPath: '/'
    },
    optimization: {
      minimizer: [new TerserPlugin({extractComments: true, sourceMap: true})],
      splitChunks: {
        cacheGroups: {
          vendor: {
            test: /node_modules/,
            chunks: "all",
            name: "../../vendors/vendors",
            enforce: true
          },
          core: {
            test: /[\\/]core.*/,
            chunks: "all",
            name: "core",
            enforce: true
          }
        }
      }
    },
    devtool: isProductionMode ? 'source-map' : 'eval-source-map',
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
          exclude: /node_modules/,
          use: [
            {loader: 'style-loader'},
            {
              loader: 'css-loader',
              options: {
                modules: true
              }
            }
          ]
        },
        {
          test: /\.css$/,
          include: /node_modules/,
          use: [{loader: 'style-loader'}, {loader: 'css-loader'}]
        }
      ]
    },
    resolve: {
      alias: webpackAlias
    },
    plugins: pluginsInUse,
    devServer: {
      contentBase: path.resolve(__dirname, "../target"),
      publicPath: "/",
      progress: true,
      https: true,
      open: true,
      writeToDisk: argv && argv.writeToDisk,
      proxy: [{
        context: ['!/sockjs-node/**'],
        target: (argv && argv.server) || "https://suma-refhead-srv.mgr.suse.de",
        ws: true,
        secure: false
      }]
    },
  }]

};