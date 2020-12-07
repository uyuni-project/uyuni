module.exports = {
  "moduleNameMapper": {
    "^core/(.*)$": "<rootDir>/core/$1",
    "^components/(.*)$": "<rootDir>/components/$1",
    "^utils/(.*)$": "<rootDir>/utils/$1"
  },
  setupFiles: ["./utils/test-utils/setup.js"]
}
