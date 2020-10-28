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
    new CopyWebpackPlugin({
      patterns: [
        { from: path.resolve(__dirname, "../../javascript"), to: path.resolve(__dirname, "../dist/javascript") }
      ],
    }),
    new CopyWebpackPlugin({
      patterns: [
        { from: path.resolve(__dirname, "../../../po"), to: path.resolve(__dirname, "../dist/po") }
      ],
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
  } else {
    pluginsInUse = [
      ...pluginsInUse,
      new CopyWebpackPlugin({
        patterns: [
          { from: path.resolve(__dirname, "../../../../branding/css"), to: path.resolve(__dirname, "../dist/css") }
        ],
      }),
    ]
  }

  return [{
    entry: {
      'javascript/manager/main': './manager/index.js'
    },
    output: {
      filename: `[name].bundle.js`,
      path: path.resolve(__dirname, "../dist/" ),
      chunkFilename: 'javascript/manager/[name].bundle.js',
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
        },
        {
          test: /\.po$/,
          type: 'json',
          use: {
            loader: path.resolve(__dirname, 'loaders/po-loader.js')
          }
        }
      ]
    },
    resolve: {
      alias: webpackAlias
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
        secure: false
      }]
    },
  }]
};
