module.exports = {
  parser: "@typescript-eslint/parser",
  root: true,
  extends: [
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
    "react-hooks/rules-of-hooks": "error",
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
