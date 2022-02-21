const { po } = require("gettext-parser");

module.exports = function (source) {
  this.cacheable();
  return JSON.stringify(po.parse(source, "utf8"));
};
