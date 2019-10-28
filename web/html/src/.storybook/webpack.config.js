const webpackAlias = require('../build/webpack.alias');

module.exports = async ({ config, mode }) => {
  config.resolve.alias = {...config.resolve.alias, ...webpackAlias};
  return config;
};
