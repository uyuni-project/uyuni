module.exports = {
  parser: "@typescript-eslint/parser",
  root: true,
  extends: [
    // This requires `eslint-plugin-flowtype` but we don't actually use it, see https://github.com/facebook/create-react-app/issues/6129
    "react-app",
  ],

  plugins: [
    "react-hooks",
    "@typescript-eslint"
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
    "jsx-a11y/anchor-is-valid": "error",
    "react-hooks/rules-of-hooks": "error",
    "eqeqeq": "error",
    // TODO: Eventually this should be ["error", "always"]
    "radix": ["warn", "always"],
    // TODO: Eventually we should enforce this as well
    // "no-eq-null": "error",
    // TODO: This needs to be reworked with Typescript support in mind
    "no-use-before-define": "off",
    // See https://reactjs.org/blog/2020/09/22/introducing-the-new-jsx-transform.html#eslint
    "react/jsx-uses-react": "off",
    "react/react-in-jsx-scope": "off"
  },

  settings: {
    'import/resolver': {
      alias: {
        map: [
          ['components', './components'],
          ['core', './core'],
          ['manager', './manager'],
          ['utils', './utils'],
        ]
      }
    }
  },
}
