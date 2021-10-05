import SimpleDataProvider from "./simple-data-provider";
import PageControl from "./page-control";
import { Utils } from "utils/functions";

const TEST_DATA = [
  { first: 1, second: "angel" },
  { first: 2, second: "apple" },
  { first: 3, second: "orange" },
  { first: 4, second: "ranger" },
  { first: 5, second: "banana" },
];

test("Test filtering", () => {
  let provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first);

  // No filter function provided, should not filter
  expect(provider.getFilteredData("ang")).toEqual(TEST_DATA);

  const filterFunc = (row, crit) => row.second.includes(crit);
  provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first, filterFunc);

  const filteredData = provider.getFilteredData("ang");
  expect(filteredData).toHaveLength(3);
  expect(filteredData).toContainEqual({ first: 1, second: "angel" });
  expect(filteredData).toContainEqual({ first: 3, second: "orange" });
  expect(filteredData).toContainEqual({ first: 4, second: "ranger" });
});

test("Test getIds", (done) => {
  const filterFunc = (row, crit) => row.second.includes(crit);
  const provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first, filterFunc);

  provider.getIds((promise) =>
    promise.then((ids) => {
      expect(ids).toEqual([1, 2, 3, 4, 5]);
      done();
    })
  );
});

test("Test getIds with filtering", (done) => {
  const filterFunc = (row, crit) => row.second.includes(crit);
  const provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first, filterFunc);

  provider.getIds(
    (promise) =>
      promise.then((ids) => {
        expect(ids).toEqual([1, 3, 4]);
        done();
      }),
    "ang"
  );
});

test("Test get", (done) => {
  const provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first);
  const pageControl = new PageControl(1, 3);

  provider.get(
    (promise) =>
      promise.then(({ items, total }) => {
        expect(items).toHaveLength(3);
        expect(items).toContainEqual({ first: 1, second: "angel" });
        expect(items).toContainEqual({ first: 2, second: "apple" });
        expect(items).toContainEqual({ first: 3, second: "orange" });
        expect(total).toBe(5);
        done();
      }),
    pageControl
  );
});

test("Test get page", (done) => {
  const provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first);
  const pageControl = new PageControl(4, 3);

  provider.get(
    (promise) =>
      promise.then(({ items, total }) => {
        expect(items).toHaveLength(2);
        expect(items).toContainEqual({ first: 4, second: "ranger" });
        expect(items).toContainEqual({ first: 5, second: "banana" });
        expect(total).toBe(5);
        done();
      }),
    pageControl
  );
});

test("Test get with filtering", (done) => {
  const filterFunc = (row, crit) => row.second.includes(crit);
  const provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first, filterFunc);
  const pageControl = new PageControl(1, 3, "ang");

  provider.get(
    (promise) =>
      promise.then(({ items, total }) => {
        expect(items).toHaveLength(3);
        expect(items).toContainEqual({ first: 1, second: "angel" });
        expect(items).toContainEqual({ first: 3, second: "orange" });
        expect(items).toContainEqual({ first: 4, second: "ranger" });
        expect(total).toBe(3);
        done();
      }),
    pageControl
  );
});

test("Test get with sorting", (done) => {
  const comparators = {
    first: Utils.sortByNumber,
    second: Utils.sortByText,
  };
  const filterFunc = (row, crit) => row.second.includes(crit);
  const provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first, filterFunc, comparators);
  const pageControl = new PageControl(1, 3, "an", "second", 1);

  provider.get(
    (promise) =>
      promise.then(({ items, total }) => {
        expect(items).toHaveLength(3);
        expect(items).toEqual([
          { first: 1, second: "angel" },
          { first: 5, second: "banana" },
          { first: 3, second: "orange" },
        ]);
        expect(total).toBe(4);
        done();
      }),
    pageControl
  );
});

test("Test get all", (done) => {
  const filterFunc = (row, crit) => row.second.includes(crit);
  const provider = new SimpleDataProvider(Array.from(TEST_DATA), (o) => o.first, filterFunc);

  // No page control provided
  provider.get((promise) =>
    promise.then(({ items, total }) => {
      expect(items).toEqual(TEST_DATA);
      expect(total).toBe(5);
      done();
    })
  );
});
