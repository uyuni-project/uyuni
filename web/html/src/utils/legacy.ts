/**
 * DEPRECATED: Do **NOT** use this function for new code
 *
 * This function exists solely to account for old code that relies on equalities
 * such as `"1" == 1` or `0 == ""` and similar JS nonsense. This can happen when
 * strings and numbers are used interchangably with abandon.
 *
 * If you work on code like this, please figure out what the correct types are
 * and replace the use of this function with `===`.
 *
 * This is not a safe switch to do with global find and replace due to the above
 * equalities handling differently in strict equality. Please be sure to always
 * test your changes.
 */
export function DEPRECATED_unsafeEquals(a: any, b: any) {
  // eslint-disable-next-line eqeqeq
  return a == b;
}
