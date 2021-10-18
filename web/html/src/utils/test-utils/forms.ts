import { screen, queryHelpers } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as selectEvent from "react-select-event";

// react-select testing utilities: https://github.com/romgain/react-select-event#api
export * from "react-select-event";

const queryAllByName = queryHelpers.queryAllByAttribute.bind(null, "name");

// This isn't universal, but suffices for this context
function isInput(input: HTMLElement): input is HTMLInputElement {
  return input && Object.prototype.hasOwnProperty.call(input, "value");
}

/** Get all the values of fields of a field name within a form.
 * This is useful to get the values of a Select in multi mode */
export const getFieldValuesByName = (formName: string, fieldName: string) => {
  const form = screen.getByTitle(formName);
  const fields = queryAllByName(form, fieldName);
  return fields.filter(isInput).map((field) => field.value);
};

const asyncAnimationFrame = () => new Promise((resolve) => window.requestAnimationFrame(() => resolve(undefined)));

/**
 * This is a usable alternative for @testing-library/user-event's `type()`.
 * The library's `type()` is non-deterministic without a delay and prohibitively
 * slow even if you use Number.MIN_VALUE as the delay value.
 * Instead we use the `paste()` method which inserts the full text in one go and
 * pretend there is no difference.
 */
export const type = async <T extends HTMLElement>(
  elementOrPromiseOfElement: T | Promise<T>,
  text: string,
  append = false
) => {
  const target = await elementOrPromiseOfElement;
  if (!append) {
    userEvent.clear(target);
  }
  userEvent.paste(target, text, undefined);

  /**
   * `window.requestAnimationFrame` mandatory to ensure we don't proceed until
   * the UI has updated, expect non-deterministic results without this.
   */
  return asyncAnimationFrame();
};

type SelectParams = Parameters<typeof selectEvent.select>;
type InputType = SelectParams[0];
type WrappedInputType = InputType | null | Promise<InputType | null>;
type OptionType = SelectParams[1];
type ConfigType = SelectParams[2];
/** Select a given option in a react-select dropdown, e.g. `select(screen.getByLabelText(/Foo/), "Bar")` */
export const select = async (elementOrPromiseOfElement: WrappedInputType, option: OptionType, config?: ConfigType) => {
  const target = await elementOrPromiseOfElement;
  if (!target) {
    throw new TypeError("Found no target to select via " + target);
  }
  await selectEvent.select(target, option, Object.assign({}, { container: document.body }, config));
  return asyncAnimationFrame();
};
