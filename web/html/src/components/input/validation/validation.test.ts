import { Validation } from "./validation";

const errorMessage = "error message";

describe("validation", () => {
  test("minLength string", () => {
    const validator = Validation.minLength(3, errorMessage);

    expect(validator("foo")).toEqual(undefined);
    expect(validator("fo")).toEqual(errorMessage);
  });

  test("minLength object", () => {
    const validator = Validation.minLength(3, errorMessage);

    expect(
      validator({
        foo: "foo",
        bar: "bar",
        tea: "tea",
      })
    ).toEqual(undefined);

    expect(
      validator({
        foo: "foo",
        bar: "ba",
        tea: "tea",
      })
    ).toEqual(errorMessage);
  });

  test("maxLength string", () => {
    const validator = Validation.maxLength(3, errorMessage);

    expect(validator("foo")).toEqual(undefined);
    expect(validator("fooo")).toEqual(errorMessage);
  });

  test("maxLength object", () => {
    const validator = Validation.maxLength(3, errorMessage);

    expect(
      validator({
        foo: "foo",
        bar: "bar",
        tea: "tea",
      })
    ).toEqual(undefined);

    expect(
      validator({
        foo: "foo",
        bar: "barr",
        tea: "tea",
      })
    ).toEqual(errorMessage);
  });

  test("isInt", () => {
    const validator = Validation.isInt();

    // If you want the value to be required, set the `required` flag instead
    expect(validator("")).toEqual(true);
    expect(validator("0")).toEqual(true);
    expect(validator("42")).toEqual(true);
    expect(validator("42.")).toEqual(false);
    expect(validator("4.2")).toEqual(false);
    expect(validator("0x1")).toEqual(false);
    expect(validator("foo")).toEqual(false);
  });

  test("matches", () => {
    // TODO: Implement
  });
});
