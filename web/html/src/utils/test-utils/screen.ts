import { getDefaultNormalizer, queryHelpers, Screen, screen as rawScreen } from "@testing-library/react";

// Utility type, if a function TargetFunction returns a Promise, return an intersection with Promise<T>, otherwise with T
type ReturnFromWith<TargetFunction extends (...args: any[]) => any, T> =
  ReturnType<TargetFunction> extends Promise<unknown>
    ? ReturnType<TargetFunction> & Promise<T>
    : ReturnType<TargetFunction> & T;

/**
 * Testing-library doesn't ship generic versions of queries so sometimes it's a pain to annotate what we know comes out of them.
 * This simply adds the option to annotate the expected output of queries conveniently where needed.
 * This is still a bit of a hack though, if you have a better solution please feel free to switch this out.
 */
type GenericScreen = {
  [Key in keyof Screen]: Screen[Key] extends (...args: any[]) => any
    ? <T>(...args: Parameters<Screen[Key]>) => ReturnFromWith<Screen[Key], T>
    : Screen[Key];
};

/**
 * Labels (as in `components/input/Label.tsx`) often have a required suffix "*" and a generic display suffix ":".
 * Instead of cluttering the DOM there, we account for it in the tests by using a normalizer here.
 * See: https://testing-library.com/docs/queries/about/#normalization
 */
const defaultNormalizer = getDefaultNormalizer();
const labelNormalizer = (input: string) => {
  return defaultNormalizer(input)
    .replace(/:$/, "") // Remove trailing ":"
    .replace(/ \*$/, ""); // Remove trailing " *"
};

// Override `screen.getByLabelText`
const getByLabelText = rawScreen.getByLabelText;
type GetByLabelTextArgs = Parameters<typeof rawScreen.getByLabelText>;

// Helpers for querying by testid so we use similar selectors to Cucumber, see https://testing-library.com/docs/dom-testing-library/api-custom-queries/
const queryByTestId = queryHelpers.queryByAttribute.bind(null, "data-testid");
const queryAllByTestId = queryHelpers.queryAllByAttribute.bind(null, "data-testid");

function getByTestId(...[container, id, ...rest]: Parameters<typeof getAllByTestId>) {
  const result = getAllByTestId(container, id, ...rest);
  if (result.length > 1) {
    throw queryHelpers.getElementError(`Found multiple elements with the [data-testid="${id}"]`, container);
  }
  return result[0];
}
function getAllByTestId(...[container, id, ...rest]: Parameters<typeof queryAllByTestId>) {
  const els = queryAllByTestId(container, id, ...rest);
  if (!els.length) {
    throw queryHelpers.getElementError(`Unable to find an element by: [data-testid="${id}"]`, container);
  }
  return els;
}

const additionalQueries = {
  queryByTestId,
  queryAllByTestId,
  getByTestId,
  getAllByTestId,
};

Object.assign(rawScreen, {
  getByLabelText: (...[text, options]: GetByLabelTextArgs) => {
    options ??= {};
    options.normalizer ??= labelNormalizer;
    return getByLabelText(text, options);
  },
  additionalQueries,
} as Partial<Screen>);

const screen = rawScreen as GenericScreen & typeof additionalQueries;

export { screen };
