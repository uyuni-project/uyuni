// https://github.com/chriso/validator.js
import validator from "validator";

const f =
  (fn) =>
  (...args) =>
  (str) =>
    fn(str, ...args);
const validations: Record<string, any> = {};

Object.keys(validator).forEach((v) => {
  if (typeof validator[v] === "function") {
    validations[v] = f(validator[v]);
  }
});

export default validations;
