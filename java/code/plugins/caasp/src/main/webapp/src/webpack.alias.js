const path = require('path');

module.exports = {
  components: path.resolve(__dirname, 'spacewalk-web/components/'),
    core: path.resolve(__dirname, 'spacewalk-web/core/'),
    utils: path.resolve(__dirname, 'spacewalk-web/utils/'),
    "jquery": path.resolve(__dirname, './inject.global.jquery.js'),
};

