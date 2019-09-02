const path = require('path');

module.exports = {
  components: path.resolve(__dirname, '../components/'),
    core: path.resolve(__dirname, '../core/'),
    utils: path.resolve(__dirname, '../utils/'),
    "jquery": path.resolve(__dirname, './inject.global.jquery.js'),
};
