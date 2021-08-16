import { getUrlParam } from "./url";

describe("URL utils", () => {
  const setSearchString = (value: string) => {
    window.history.replaceState(undefined, "", `/?${value}`);
  };

  test("retrieves strings", () => {
    setSearchString("fooBar=foo%20%26%20bar%20with%20symbols%20%3F!%40%23%24%25%5E%26*()_%2B%3D%22");
    expect(getUrlParam("fooBar")).toEqual('foo & bar with symbols ?!@#$%^&*()_+="');
  });

  test("retrieves numbers", () => {
    setSearchString("fooBar=1234567890");
    expect(getUrlParam("fooBar", Number)).toEqual(1234567890);
  });

  test("yields undefined for missing params", () => {
    setSearchString("foo=foo");
    expect(getUrlParam("bar")).toEqual(undefined);
    expect(getUrlParam("bar", Number)).toEqual(undefined);
  });

  test("yields undefined for params with no value", () => {
    setSearchString("foo=");
    expect(getUrlParam("foo")).toEqual(undefined);
    expect(getUrlParam("foo", Number)).toEqual(undefined);

    setSearchString("bar");
    expect(getUrlParam("bar")).toEqual(undefined);
    expect(getUrlParam("bar", Number)).toEqual(undefined);
  });
});
