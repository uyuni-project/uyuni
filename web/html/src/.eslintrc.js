module.exports = {
    parser: "babel-eslint",
    extends: [
        "airbnb"
    ],

    plugins: [
        "flowtype-errors"
    ],

    rules: {
      "flowtype-errors/show-errors": "error",
      "flowtype-errors/show-warnings": "warn",
    }

}
