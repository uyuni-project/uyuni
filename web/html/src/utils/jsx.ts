/**
 * Flatten `input` into a valid `className` string for JSX after removing falsy values.
 * Useful when you have numerous conditional classes.
 *
 * For example:
 *  - `flatten(["foo", "bar"]) === "foo bar"`
 *  - `flatten([false && "foo", true && "bar", undefined]) === "bar"`
 */
export function flatten(
  input: string | false | undefined | null | (string | false | undefined | null)[]
): string | undefined {
  if (Array.isArray(input)) {
    return input.filter((item) => typeof item === "string").join(" ") || undefined;
  }
  return input || undefined;
}
