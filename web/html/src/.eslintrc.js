module.exports = {
  parser: "@typescript-eslint/parser",
  root: true,
  extends: [
    "react-app",
  ],

  plugins: [
    "@typescript-eslint",
    "flowtype-errors",
    "react-hooks",
  ],

  env: {
    "browser": true,
  },

  "globals": {
    "t": true,
    "module": true,
    "jQuery": true,
  },

  rules: {
    "flowtype-errors/show-errors": "error",
    "flowtype-errors/show-warnings": "warn",
    "react-hooks/rules-of-hooks": "error",
  },

  settings: {
    'import/resolver': {
      alias: {
        map: [
          ['components', './components'],
          ['core', './core'],
          ['utils', './utils'],
        ]
      }
    }
  },
}
