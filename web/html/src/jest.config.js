const { defaults } = require("jest-config");

module.exports = {
  // See https://kulshekhar.github.io/ts-jest/docs/presets#the-presets
  // Using Babel allows us to keep current Flow stuff at least somewhat working
  preset: "ts-jest/presets/js-with-babel",
  // Required for in-memory rendering with @testing-library/react
  testEnvironment: "jsdom",
  // We sometimes get slow runs in our internal infra when the load is high on tests that otherwise pass
  testTimeout: 30000,
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
    // Don't do server time sanity checks in the test env since they litter the logs but don't give any useful info there
    serverTime: undefined,
    userTimeZone: "America/Los_Angeles", // GMT-7
    userDateFormat: "YYYY-MM-DD",
    userTimeFormat: "HH:mm",
  },
  // Until tests with `async (done) => ...` are fixed, we need to use a custom runner, see https://github.com/facebook/jest/issues/11404
  testRunner: "jest-jasmine2",
};
