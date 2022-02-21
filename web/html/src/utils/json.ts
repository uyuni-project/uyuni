/**
 * This replacer is meant to be used on JSON.stringify in order to convert some ES6 types that are not
 * converted out-of-the-box, for instance, the 'Map' type.
 *
 * Examples:
 *  - JSON.stringify(new Map([['a', 1]])) will return '{}'
 *  - JSON.stringify(new Map([['a', 1]]), replacer) will return '{"a":1}'
 */
function replacer(key, value) {
  if (value instanceof Map) {
    return Object.fromEntries(value);
  } else {
    return value;
  }
}

export { replacer };
