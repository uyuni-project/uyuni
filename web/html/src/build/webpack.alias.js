const path = require("path");

module.exports = {
  components: path.resolve(__dirname, "../components/"),
  core: path.resolve(__dirname, "../core/"),
  manager: path.resolve(__dirname, "../manager/"),
  utils: path.resolve(__dirname, "../utils/"),
  jquery: path.resolve(__dirname, "./inject.global.jquery.js"),
  // Support HRM with hooks, see https://github.com/gaearon/react-hot-loader#hot-loaderreact-dom
  "react-dom": "@hot-loader/react-dom",
};
