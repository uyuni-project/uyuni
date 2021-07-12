import { replacer } from "./json";

test("check Map stringify", () => {
  expect(JSON.stringify(new Map([["a", 1]]), replacer)).toEqual('{"a":1}');
});
