module.exports = {
  extends: "./.eslintrc.js",
  rules: {
    // Make stylistic issues fail production lint
    "prettier/prettier": "error",
  },
};
