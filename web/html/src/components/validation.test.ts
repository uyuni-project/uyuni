import Validation from "components/validation";

describe("validation", () => {
  describe("matches", () => {});

  describe("isInt", () => {
    test("empty inputs", () => {
      expect(Validation.isInt("")).toEqual(false);
      expect(Validation.isInt(null)).toEqual(false);
      expect(Validation.isInt(undefined)).toEqual(false);
      expect(Validation.isInt(NaN as any)).toEqual(false);

      expect(Validation.isInt({ gt: 0 })("")).toEqual(false);
      expect(Validation.isInt({ gt: 0 })(null)).toEqual(false);
      expect(Validation.isInt({ gt: 0 })(undefined)).toEqual(false);

      expect(Validation.isInt({ gt: 0, lt: 255 })("")).toEqual(false);
      expect(Validation.isInt({ gt: 0, lt: 255 })(null)).toEqual(false);
      expect(Validation.isInt({ gt: 0, lt: 255 })(undefined)).toEqual(false);

      expect(Validation.isInt({ lt: 255 })("")).toEqual(false);
      expect(Validation.isInt({ lt: 255 })(null)).toEqual(false);
      expect(Validation.isInt({ lt: 255 })(undefined)).toEqual(false);
    });

    test("basic inputs", () => {
      expect(Validation.isInt("0")).toEqual(true);
      expect(Validation.isInt("-0")).toEqual(true);
      expect(Validation.isInt("1")).toEqual(true);
      expect(Validation.isInt("-1")).toEqual(true);

      expect(Validation.isInt("0.5")).toEqual(false);
      expect(Validation.isInt("0.9999999999")).toEqual(false);
      expect(Validation.isInt("-0.5")).toEqual(false);
      expect(Validation.isInt("abc")).toEqual(false);
      expect(Validation.isInt("1.2.3.4")).toEqual(false);

      expect(Validation.isInt(Number.EPSILON.toString())).toEqual(false);
      expect(Validation.isInt((1 - Number.EPSILON).toString())).toEqual(false);

      expect(Validation.isInt(Number.MAX_SAFE_INTEGER.toString())).toEqual(true);
      expect(Validation.isInt((-1 * Number.MAX_SAFE_INTEGER).toString())).toEqual(true);
    });

    test("gt ranges", () => {
      expect(Validation.isInt({ gt: 0 })("-1")).toEqual(false);
      expect(Validation.isInt({ gt: 0 })("-0.5")).toEqual(false);
      expect(Validation.isInt({ gt: 0 })("0")).toEqual(false);
      expect(Validation.isInt({ gt: 0 })("0.5")).toEqual(false);
      expect(Validation.isInt({ gt: 0 })("1")).toEqual(true);
    });

    test("lt ranges", () => {
      expect(Validation.isInt({ lt: 0 })("-1")).toEqual(true);
      expect(Validation.isInt({ lt: 0 })("-0.5")).toEqual(false);
      expect(Validation.isInt({ lt: 0 })("0")).toEqual(false);
      expect(Validation.isInt({ lt: 0 })("0.5")).toEqual(false);
      expect(Validation.isInt({ lt: 0 })("1")).toEqual(false);
    });

    test("gt lt ranges", () => {
      expect(Validation.isInt({ gt: 0, lt: 3 })("0")).toEqual(false);
      expect(Validation.isInt({ gt: 0, lt: 3 })("1")).toEqual(true);
      expect(Validation.isInt({ gt: 0, lt: 3 })("2")).toEqual(true);
      expect(Validation.isInt({ gt: 0, lt: 3 })("3")).toEqual(false);
    });
  });

  describe("isFloat", () => {
    test("empty inputs", () => {
      expect(Validation.isFloat("")).toEqual(false);
      expect(Validation.isFloat(null)).toEqual(false);
      expect(Validation.isFloat(undefined)).toEqual(false);
      expect(Validation.isFloat(NaN as any)).toEqual(false);
    });

    test("basic inputs", () => {
      expect(Validation.isFloat("0")).toEqual(true);
      expect(Validation.isFloat("-0")).toEqual(true);
      expect(Validation.isFloat("0.5")).toEqual(true);
      expect(Validation.isFloat("-0.5")).toEqual(true);
      expect(Validation.isFloat("1")).toEqual(true);
      expect(Validation.isFloat("-1")).toEqual(true);
      expect(Validation.isFloat("abc")).toEqual(false);
      expect(Validation.isFloat("1.2.3.4")).toEqual(false);

      expect(Validation.isFloat(Number.EPSILON.toString())).toEqual(true);
      expect(Validation.isFloat((1 - Number.EPSILON).toString())).toEqual(true);

      expect(Validation.isFloat(Number.MAX_VALUE.toString())).toEqual(true);
      expect(Validation.isFloat((-1 * Number.MAX_VALUE).toString())).toEqual(true);
    });
  });

  describe("isUrl", () => {
    test("empty inputs", () => {
      expect(Validation.isURL("")).toEqual(false);
      expect(Validation.isURL(null)).toEqual(false);
      expect(Validation.isURL(undefined)).toEqual(false);
      expect(Validation.isURL(NaN as any)).toEqual(false);
    });

    test("basic inputs", () => {
      expect(Validation.isURL("http://")).toEqual(false);
      expect(Validation.isURL("https://")).toEqual(false);
      expect(Validation.isURL("ftp://example.com")).toEqual(false);

      expect(Validation.isURL("http://example.com")).toEqual(true);
      expect(Validation.isURL("https://example.com")).toEqual(true);
    });
  });
});
