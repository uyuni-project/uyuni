module.exports = {
  parser: "babel-eslint",
  root: true,
  extends: [
    "react-app",
  ],

  plugins: [
    "flowtype-errors", "react-hooks"
  ],

  env: {
    "browser": true,
  },

  "globals": {
    "t": true,
    "module": true,
    "$": true,
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
