import {Utils} from "./functions";

test("check order by date function'", () => {
  const eRaw = { id: "e", date: "11/11/1983 23:59:59"};
  const aRaw = { id: "a", date: "11/11/1983 1:10:10 CEST"};
  const bRaw = { id: "b", date: "1983/11/11 11:11:11 CEST"};
  const dRaw = { id: "d", date: "1983/11/11 23:23:23"};
  const cRaw = { id: "c", date: "11/11/1983 16:16:16 CEST"};

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
})