import { flatten } from "./jsx";

describe("jsx flatten", () => {
  test("returns regular strings unmodified", () => {
    expect(flatten("foo")).toEqual("foo");
  });

  test("returns undefined on falsy values", () => {
    expect(flatten("")).toEqual(undefined);
    expect(flatten(false)).toEqual(undefined);
    expect(flatten(undefined)).toEqual(undefined);
    expect(flatten([])).toEqual(undefined);
  });

  test("joins arrays of strings", () => {
    expect(flatten(["foo", "bar"])).toEqual("foo bar");
  });

  test("removes falsy values from arrays", () => {
    expect(flatten([false, "foo", undefined, "bar", null])).toEqual("foo bar");
  });
});
