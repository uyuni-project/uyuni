const { defaults } = require("jest-config");

module.exports = {
  // See https://kulshekhar.github.io/ts-jest/docs/presets#the-presets
  // Using Babel allows us to keep current Flow stuff at least somewhat working
  preset: "ts-jest/presets/js-with-babel",
  // Required for in-memory rendering with @testing-library/react
  testEnvironment: "jsdom",
  verbose: true,
  moduleFileExtensions: [...defaults.moduleFileExtensions, "ts", "tsx"],
  moduleNameMapper: {
    "^components/(.*)$": "<rootDir>/components/$1",
    "^core/(.*)$": "<rootDir>/core/$1",
    "^manager/(.*)$": "<rootDir>/manager/$1",
    "^utils/(.*)$": "<rootDir>/utils/$1",
    "\\.(css|less)$": "identity-obj-proxy",
  },
  modulePaths: ["<rootDir>"],
  moduleDirectories: ["node_modules"],
  setupFiles: ["./utils/test-utils/setup.ts"],
  globals: {
    ...defaults.globals,
    csrfToken: "TEST" // TODO: Any better ideas?
  }
};
