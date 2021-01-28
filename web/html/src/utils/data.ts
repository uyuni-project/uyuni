function getObjectValue(obj: object, path: string, defaultValue: any) {
  const pos = path.indexOf('.');
  if (pos > 0) {
    const member_name = path.substring(0, pos);
    const path_rest = path.substring(pos + 1);

    return getObjectValue(obj[member_name] || {}, path_rest, defaultValue);
  }
  return obj[path] != null ? obj[path] : defaultValue;
}

/**
 * Extract a value from an object using dot-separated property names.
 * For instance getting 'foo.bar.baz' value will return data.foo.bar.baz
 * if it exists, `defaultValue` otherwise.
 */
export function getValue(data: object, path: string, defaultValue: any) {
  return getObjectValue(data || {}, path, defaultValue);
}
