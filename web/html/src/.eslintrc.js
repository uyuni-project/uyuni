module.exports = {
    parser: "babel-eslint",
    root: true,
    extends: [
        "airbnb",
        "plugin:you-dont-need-lodash-underscore/compatible",
    ],

    plugins: [
        "flowtype-errors", "you-dont-need-lodash-underscore"
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
      "no-underscore-dangle": "off",
      "react/jsx-filename-extension": [1, { "extensions": [".js", ".jsx"] }],
      "react/destructuring-assignment": "off",
      "jsx-a11y/label-has-for": "off"
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
