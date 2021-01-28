import * as data from 'utils/data'

test("test getValue", () => {
  const obj = {
    foo: 1,
    bar: {
      baz: "hello",
    },
  }
  expect(data.getValue(obj, "foo", 0)).toEqual(1);
  expect(data.getValue(obj, "bar.baz", 0)).toEqual("hello");
  expect(data.getValue(obj, "foobar", 0)).toEqual(0);
})
