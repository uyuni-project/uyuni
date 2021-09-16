module.exports = {
  "moduleNameMapper": {
    "^core/(.*)$": "<rootDir>/core/$1",
    "^components/(.*)$": "<rootDir>/components/$1",
    "^utils/(.*)$": "<rootDir>/utils/$1"
  },
  setupFiles: ["./utils/test-utils/setup.js"],
  // We sometimes get slow runs in our internal infra when the load is high on tests that otherwise pass
  testTimeout: 30000
}
