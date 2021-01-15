module.exports = {
  parser: "@typescript-eslint/parser",
  root: true,
  extends: [
    "react-app",
  ],

  plugins: [
    "react-hooks",
    "@typescript-eslint",
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
    "react-hooks/rules-of-hooks": "error",
    // TODO: This needs to be reworked with Typescript support in mind
    "no-use-before-define": "off"
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
