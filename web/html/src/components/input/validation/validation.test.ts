import { Validation } from "./validation";

const errorMessage = "error message";

describe("validation", () => {
  test("matches", () => {
    const validator = Validation.matches(/foo/, errorMessage);

    expect(validator("")).toEqual(undefined);
    expect(validator("-")).toEqual(errorMessage);
    expect(validator("-foo")).toEqual(undefined);
    expect(validator("-foo-")).toEqual(undefined);
    expect(validator("-fo-")).toEqual(errorMessage);
  });

  test("minLength string", () => {
    const validator = Validation.minLength(3, errorMessage);

    // Here and elsewhere, if you want the value to be required, set the `required` flag instead
    expect(validator("")).toEqual(undefined);
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

    expect(validator("")).toEqual(undefined);
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
    const validator = Validation.isInt(errorMessage);

    expect(validator("")).toEqual(undefined);
    expect(validator("0")).toEqual(undefined);
    expect(validator("42")).toEqual(undefined);
    expect(validator("42.")).toEqual(errorMessage);
    expect(validator("4.2")).toEqual(errorMessage);
    expect(validator("0x1")).toEqual(errorMessage);
    expect(validator("foo")).toEqual(errorMessage);
  });

  test("min", () => {
    const validator = Validation.min(7, errorMessage);

    expect(validator("")).toEqual(undefined);
    expect(validator("6")).toEqual(errorMessage);
    expect(validator("7")).toEqual(undefined);
    expect(validator("8")).toEqual(undefined);
  });

  test("max", () => {
    const validator = Validation.max(7, errorMessage);

    expect(validator("")).toEqual(undefined);
    expect(validator("6")).toEqual(undefined);
    expect(validator("7")).toEqual(undefined);
    expect(validator("8")).toEqual(errorMessage);
  });

  test("intRange", () => {
    const validator = Validation.range(3, 5, errorMessage);

    expect(validator("")).toEqual(undefined);
    expect(validator("1.5")).toEqual(errorMessage);
    expect(validator("2")).toEqual(errorMessage);
    expect(validator("3")).toEqual(undefined);
    expect(validator("4")).toEqual(undefined);
    expect(validator("4.5")).toEqual(errorMessage);
    expect(validator("5")).toEqual(undefined);
    expect(validator("6")).toEqual(errorMessage);
  });
});
