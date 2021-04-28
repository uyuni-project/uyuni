import * as data from "utils/data";

describe("object getValue", () => {
  test("simple get", () => {
    const obj = {
      foo: 1,
      bar: {
        baz: "hello",
      },
    };
    expect(data.getValue(obj, "foo", 0)).toEqual(1);
    expect(data.getValue(obj, "bar.baz", 0)).toEqual("hello");
  });

  test("default value", () => {
    const obj = {};
    expect(data.getValue(obj, "foo.bar.baz", "default")).toEqual("default");
  });

  test("default value doesn't overwrite nullish values", () => {
    const obj = {
      foo: {
        bar: {
          baz: null,
        },
      },
    };
    expect(data.getValue(obj, "foo.bar.baz", "default")).toEqual(null);
  });

  test("nested array get", () => {
    const obj = {
      foo: {
        bar: [
          {
            baz: {
              tea: 1,
            },
          },
          {
            baz: {
              tea: 2,
            },
          },
          {
            baz: {
              tea: 3,
            },
          },
        ],
      },
    };
    expect(data.getValue(obj, "foo.bar.baz.tea", "default")).toEqual([1, 2, 3]);
  });
});
