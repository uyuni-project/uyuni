const webpackAlias = require('../build/webpack.alias');
const path = require('path');

module.exports = async ({ config, mode }) => {
  config.resolve.alias = {...config.resolve.alias, ...webpackAlias};
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
  );
  return config;
};
