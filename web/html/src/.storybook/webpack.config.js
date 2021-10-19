const webpackAlias = require("../build/webpack.alias");
const path = require("path");

module.exports = async ({ config, mode }) => {
  config.resolve.alias = { ...config.resolve.alias, ...webpackAlias };
  config.resolve.extensions = [".ts", ".tsx", ".js", ".jsx", ".json"];
  config.module.rules.push(
    {
      test: /\.less$/,
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
              rootpath: path.resolve(__dirname, "../../../../branding/css"),
            },
          },
        },
      ],
    },
    {
      test: /\.(ts|js)x?$/,
      exclude: /node_modules/,
      use: {
        loader: "esbuild-loader",
        options: {
          loader: "tsx",
          target: "es2015",
        },
      },
    }
  );
  return config;
};
