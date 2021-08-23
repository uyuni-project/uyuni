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
  setupFiles: ["./utils/test-utils/setup/index.ts"],
  globals: {
    // These are simply sufficiently different so it's easy to check outputs
    serverTimeZone: "Asia/Tokyo", // GMT+9
    serverTime: "2020-01-31T08:00:00.000+09:00",
    userTimeZone: "America/Los_Angeles", // GMT-7
  },
};
