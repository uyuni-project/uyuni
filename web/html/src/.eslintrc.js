module.exports = {
    parser: "babel-eslint",
    root: true,
    extends: [
        "airbnb"
    ],

    plugins: [
        "flowtype-errors"
    ],

    env: {
      "browser": true,
    },

    "globals": {
      "t": true,
    },

    rules: {
      "flowtype-errors/show-errors": "error",
      "flowtype-errors/show-warnings": "warn",
      "import/no-extraneous-dependencies": ["error", {"packageDir": "../../../susemanager-frontend/susemanager-nodejs-sdk-devel/"}],
      "max-len": ['error', 120, 2, {
        ignoreUrls: true,
        ignoreComments: false,
        ignoreRegExpLiterals: true,
        ignoreStrings: true,
        ignoreTemplateLiterals: true,
      }],
      "react/jsx-filename-extension": [1, { "extensions": [".js", ".jsx"] }],
      "react/destructuring-assignment": "off",
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
