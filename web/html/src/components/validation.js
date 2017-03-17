'use strict';

// https://github.com/chriso/validator.js

const Validator = require("validator");
const f = (fn) => (...args) => (str) => fn(str, ...args);
const validations = {};

Object.keys(Validator).forEach(v => {
    if(typeof Validator[v] === 'function') {
        validations[v] = f(Validator[v])
    }
});

module.exports = validations;
