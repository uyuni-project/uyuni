const path = require("path");

const webpackAlias = require("../build/webpack.alias");

module.exports = {
  entry: "./manager/index.ts",
  resolve: {
    alias: {
      ...webpackAlias,
    },
    extensions: [".ts", ".tsx", ".js", ".jsx", ".json"],
    symlinks: false,
    fallback: { path: require.resolve("path-browserify") },
  },
  module: {
    rules: [
      {
        test: /\.(css|less)$/,
        use: [
          {
            loader: "style-loader",
          },
          {
            loader: "css-loader",
          },
          {
            loader: "less-loader",
            options: {
              lessOptions: {
                rootpath: path.resolve(__dirname, "../branding/css"),
              },
            },
          },
        ],
      },
      {
        test: /\.(ts|js)x?$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
        },
      },
      {
        test: /\.po$/,
        type: "json",
        use: {
          loader: path.resolve(__dirname, "../build/loaders/po-loader.js"),
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
};
