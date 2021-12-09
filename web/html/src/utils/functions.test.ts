import { Utils } from "./functions";

test("check order by date function'", () => {
  const eRaw = { id: "e", date: "11/11/1983 23:59:59" };
  const aRaw = { id: "a", date: "11/11/1983 1:10:10 CEST" };
  const bRaw = { id: "b", date: "1983/11/11 11:11:11 CEST" };
  const dRaw = { id: "d", date: "1983/11/11 23:23:23" };
  const cRaw = { id: "c", date: "11/11/1983 16:16:16 CEST" };

  let arr = [eRaw, aRaw, bRaw, dRaw, cRaw];
  const sortColumnKey = "date";
  const sortDirection = 1;

  // sort ASC
  arr = arr.sort((a, b) => Utils.sortByDate(a, b, sortColumnKey, sortDirection));
  expect(arr[0]).toEqual(aRaw);
  expect(arr[1]).toEqual(bRaw);
  expect(arr[2]).toEqual(cRaw);
  expect(arr[3]).toEqual(dRaw);
  expect(arr[4]).toEqual(eRaw);

  // sort DESC
  arr = arr.sort((a, b) => Utils.sortByDate(a, b, sortColumnKey, -sortDirection));
  expect(arr[4]).toEqual(aRaw);
  expect(arr[3]).toEqual(bRaw);
  expect(arr[2]).toEqual(cRaw);
  expect(arr[1]).toEqual(dRaw);
  expect(arr[0]).toEqual(eRaw);
});

describe("capitalize", () => {
  test("simple cases", () => {
    expect(Utils.capitalize("foo")).toEqual("Foo");
    expect(Utils.capitalize("foo-bar")).toEqual("Foo Bar");
    expect(Utils.capitalize("fooBar")).toEqual("Foobar");
  });
  test("acronym cases", () => {
    expect(Utils.capitalize("FOO")).toEqual("FOO");
    expect(Utils.capitalize("FO-O")).toEqual("FO-O");
  });
});

describe("cancelable", () => {
  const { cancelable } = Utils;

  test("has a promise and cancel property for backwards compatibility", () => {
    const instance = cancelable(new Promise(() => undefined));
    expect(Object.prototype.hasOwnProperty.call(instance, "promise")).toEqual(true);
    expect(Object.prototype.hasOwnProperty.call(instance, "cancel")).toEqual(true);
  });

  test("instance returns a proper promise, not just a promise-like", () => {
    const instance = cancelable(new Promise(() => undefined));
    expect(instance instanceof Promise).toEqual(true);
  });

  test("has generic chainable properties like finally", () => {
    const instance = cancelable(new Promise(() => undefined));
    expect(instance.finally).toBeDefined();
  });

  test("chaining off the promise property", async () => {
    const onSuccess = jest.fn();
    const onCancel = jest.fn();
    let resolve: (value: void) => void;
    const promise = new Promise((r) => (resolve = r)).then(() => onSuccess());

    const instance = cancelable(promise, onCancel);
    setTimeout(() => resolve(), 100);
    await instance.promise;

    expect(onSuccess).toBeCalledTimes(1);
    expect(onCancel).toBeCalledTimes(0);
  });

  test("chaining directly off the instance", async () => {
    const onSuccess = jest.fn();
    const onCancel = jest.fn();
    let resolve: (value: void) => void;
    const promise = new Promise((r) => (resolve = r)).then(() => onSuccess());

    const instance = cancelable(promise, onCancel);
    setTimeout(() => resolve(), 100);
    await instance;

    expect(onSuccess).toBeCalledTimes(1);
    expect(onCancel).toBeCalledTimes(0);
  });

  test("cancelling works", async (done) => {
    const onSuccess = jest.fn();
    const onCancel = () => {
      expect(onSuccess).toBeCalledTimes(0);
      done();
    };
    const promise = new Promise(() => undefined).then(() => onSuccess());

    const instance = cancelable(promise, onCancel);
    setTimeout(() => instance.cancel(), 100);
  });
});
