const path = require("path");

module.exports = {
  title: "Component demos",
  components: "components/**/*.{ts,tsx}",
  getExampleFilename(filePath) {
    return filePath.replace(/\.tsx?$/, ".stories.md");
  },
  skipComponentsWithoutExample: true,
  require: [path.resolve(__dirname, "styleguide/setup.js"), path.resolve(__dirname, "styleguide/globals.js")],
  template: require("./styleguide/template.js"),
  // exampleMode: "expand",
  webpackConfig: require("./styleguide/webpack.config.js"),
};
