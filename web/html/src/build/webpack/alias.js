const path = require("path");

module.exports = {
  components: path.resolve(__dirname, "../../components/"),
  core: path.resolve(__dirname, "../../core/"),
  manager: path.resolve(__dirname, "../../manager/"),
  utils: path.resolve(__dirname, "../../utils/"),
  branding: path.resolve(__dirname, "../../branding/"),
  jquery: path.resolve(__dirname, "./inject.global.jquery.js"),
};
