const { defaults } = require("jest-config");

module.exports = {
  verbose: true,
  moduleFileExtensions: [...defaults.moduleFileExtensions, "ts", "tsx"],
  moduleNameMapper: {
    "^core/(.*)$": "<rootDir>/core/$1",
    "^components/(.*)$": "<rootDir>/components/$1",
    "^utils/(.*)$": "<rootDir>/utils/$1",
  },
  modulePaths: ["<rootDir>"],
  moduleDirectories: ["node_modules"],
  setupFiles: ["./utils/test-utils/setup.js"],
};
