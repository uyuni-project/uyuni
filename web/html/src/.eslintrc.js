module.exports = {
    parser: "babel-eslint",
    extends: [
//        "plugin:flowtype/recommended"
    ],

    plugins: [
//        "flowtype",
        "flowtype-errors"
    ],

    rules: {
      "flowtype-errors/show-errors": 2,
      "flowtype-errors/show-warnings": 1
    },

//    settings: {
//      flowtype: {
//        'onlyFilesWithFlowAnnotation': true
//      }
//    }
}