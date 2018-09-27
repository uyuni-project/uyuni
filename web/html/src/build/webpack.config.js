const path = require('path');
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const {pages} = require("../manager/index");
const webpack = require("webpack");
const CleanWebpackPlugin = require('clean-webpack-plugin');

const {getWebpackConfig} = require("./webpack.config.vendors");

module.exports = (env, argv) => ([
  getWebpackConfig({generateLicenses: !argv.watch }),
  {
    entry: pages,
    dependencies: ["vendors"],
    output: {
        filename: `[name].bundle.js`,
        path: path.resolve(__dirname, "../dist"),
        publicPath: '/'
    },
    devtool: argv.mode === 'development' ? "source-map" : undefined,
    optimization: {
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
    },
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
    plugins: [
      new CleanWebpackPlugin(['dist'], {  root: path.resolve(__dirname, "../")}),
      new webpack.DllReferencePlugin({
        manifest: path.resolve(__dirname, "../dist/vendors/vendors-manifest.json"),
      }),
    ]
}]);
