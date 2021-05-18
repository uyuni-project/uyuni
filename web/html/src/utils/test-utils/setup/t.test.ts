import t from "./t";

describe("test-utils t()", () => {
  test("passthrough", () => {
    expect(t("foo")).toEqual("foo");
    expect(t("undefined")).toEqual("undefined");
    expect(t("")).toEqual("");
  });

  test("tagged templates", () => {
    expect(t("foo {0} tea {1}", "bar", "cup")).toEqual("foo bar tea cup");
    expect(t("foo {0} tea {1}", undefined, 123)).toEqual("foo undefined tea 123");
    expect(t("foo {0} tea {1}", "", "")).toEqual("foo  tea ");
  });
});
