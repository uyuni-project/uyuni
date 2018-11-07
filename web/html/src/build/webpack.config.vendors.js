const path = require("path");
const webpack = require("webpack");
const vendors = require("../vendors/vendors");
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const CleanWebpackPlugin = require('clean-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');


module.exports = (env, argv) => {

  const  isProductionMode = argv && argv.mode !== "development";

  let pluginsInUse = [
    new CleanWebpackPlugin(['dist'], {  root: path.resolve(__dirname, "../")}),
    new webpack.DllPlugin({
      path: path.join(path.resolve(__dirname, "../dist/vendors"), '[name]-manifest.json'),
      name: '[name]_dll'
    }),
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
  }

  return {
    name: "vendors",
    entry: {vendors},

    output: {
      path: path.resolve(__dirname, "../dist"),
      filename: "vendors/vendors.bundle.js",
      sourceMapFilename: "[name].map",
      pathinfo: true,
      library: '[name]_dll'
    },
    optimization: {
      minimizer: [new TerserPlugin({extractComments: true})],
    },
    plugins: pluginsInUse,
  }
};
