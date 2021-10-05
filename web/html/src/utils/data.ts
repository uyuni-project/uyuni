function getObjectValue(obj: object, path: string, defaultValue?: any) {
  const value = path.split(".").reduce((target, key) => {
    if (Array.isArray(target)) {
      return target.map((item) => item?.[key]);
    }
    return target?.[key];
  }, obj);
  return typeof value !== "undefined" ? value : defaultValue;
}

/**
 * Extract a value from an object using dot-separated property names.
 * For instance getting 'foo.bar.baz' value will return data.foo.bar.baz
 * if it exists, `defaultValue` otherwise.
 */
export function getValue(data: object, path: string, defaultValue?: any) {
  return getObjectValue(data, path, defaultValue);
}
