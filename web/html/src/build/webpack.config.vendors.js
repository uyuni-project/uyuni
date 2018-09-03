const path = require("path");
const webpack = require("webpack");
const vendors = require("../vendors/vendors");
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const CleanWebpackPlugin = require('clean-webpack-plugin');

function getWebpackConfig({generateLicenses}) {
  let pluginsInUse = [
    new webpack.DllPlugin({
      path: 'dist/vendors/[name]-manifest.json',
      name: '[name]_dll'
    }),
  ];

  if(generateLicenses) {
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
    entry: {
      vendors
    },
    output:  {
      path: path.resolve(__dirname, "../dist"),
      filename: "vendors/vendors.bundle.js",
      sourceMapFilename: "[name].map",
      pathinfo: true,
      library: '[name]_dll'
    },
    plugins: pluginsInUse
  };
}
module.exports = {
  getWebpackConfig
};
