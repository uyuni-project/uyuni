const path = require('path');
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");
const {pages} = require("../manager/index");
const webpack = require("webpack");
const CleanWebpackPlugin = require('clean-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin')

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
                    loader: "babel-loader",
                    options: {
                        babelrc: false,
                        presets: [
                          ["@babel/preset-env", { "modules": false }],
                          "@babel/preset-react",
                          "@babel/preset-flow"
                        ],
                        plugins: ["@babel/plugin-proposal-class-properties"]
                    }
                }
            },
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: "eslint-loader",
                    options: {
//                      emitError: true,
                      failOnError: true
                    }
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
      new CleanWebpackPlugin(['dist'], {  root: path.resolve(__dirname, "../")}),
      new webpack.DllReferencePlugin({
        manifest: path.resolve(__dirname, "../dist/vendors/vendors-manifest.json"),
      }),
      new CopyWebpackPlugin([{ from: path.resolve(__dirname, "../../javascript"), to: path.resolve(__dirname, "../dist/javascript") }])
    ]
}]);
