/* eslint-disable */
'use strict';

// https://github.com/chriso/validator.js

const f = (fn) => (...args) => (str) => fn(str, ...args);
const validations = {};

Object.keys(validator).forEach(v => {
    if(typeof validator[v] === 'function') {
        validations[v] = f(validator[v])
    }
});

module.exports = validations;
