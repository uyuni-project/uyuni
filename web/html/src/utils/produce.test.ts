/**
 * This suite tests measurable functionality of the `immer` dependency to ensure we don't break things when upgrading from 3.1.1 to 8.0.1
 */
import produce from "./produce";

type Input = {
  foo?: number;
  bar: { value: number }[];
};

describe("immer produce", () => {
  let input: Input;
  beforeEach(() => {
    input = {
      foo: 42,
      bar: [
        {
          value: 1,
        },
        {
          value: 2,
        },
        {
          value: 3,
        },
      ],
    };
  });

  // These operations cover everything that we have in the codebase right now: assignments, deletes, modifications
  const standardProduce = (draft: Input) => {
    draft.bar = draft.bar.map((item) => {
      // We intentionally modify a nested object instead of creating a new one to test whether it's safe
      item.value = item.value * 2;
      return item;
    });

    // Delete keys
    delete draft.foo;

    // Add items
    draft.bar.unshift({ value: 1 });
  };

  test("input is unchanged", () => {
    const inputCopy = JSON.parse(JSON.stringify(input));
    produce(input, standardProduce);
    expect(input).toStrictEqual(inputCopy);
  });

  test("basic produce works", () => {
    const result = produce(input, standardProduce);

    expect(result).toStrictEqual({
      bar: [
        {
          value: 1,
        },
        {
          value: 2,
        },
        {
          value: 4,
        },
        {
          value: 6,
        },
      ],
    });
  });

  test("input descriptor is unchanged", () => {
    // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/getOwnPropertyDescriptors
    const expectedInputDescriptor = {
      foo: { value: 42, writable: true, enumerable: true, configurable: true },
      bar: {
        value: [
          {
            value: 1,
          },
          {
            value: 2,
          },
          {
            value: 3,
          },
        ],
        writable: true,
        enumerable: true,
        configurable: true,
      },
    };

    const descriptor = Object.getOwnPropertyDescriptors(input);
    // Expect descriptor to be unchanged both before produce...
    expect(descriptor).toStrictEqual(expectedInputDescriptor);

    produce(input, standardProduce);
    /// ...and after
    expect(descriptor).toStrictEqual(expectedInputDescriptor);
  });

  test("output descriptor is unchanged", () => {
    // Output should have `enumerable: true, writable: false, configurable: false`
    const expectedOutputDescriptor = {
      bar: {
        value: [
          {
            value: 1,
          },
          {
            value: 2,
          },
          {
            value: 4,
          },
          {
            value: 6,
          },
        ],
        writable: false,
        enumerable: true,
        configurable: false,
      },
    };

    const result = produce(input, standardProduce);
    const descriptor = Object.getOwnPropertyDescriptors(result);
    expect(descriptor).toStrictEqual(expectedOutputDescriptor);
  });
});
