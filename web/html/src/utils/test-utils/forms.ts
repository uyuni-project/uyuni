import { screen, queryHelpers } from "@testing-library/react";

const queryAllByName = queryHelpers.queryAllByAttribute.bind(null, "name");

// This isn't universal, but suffices for this context
function isInput(input: HTMLElement): input is HTMLInputElement {
  return input && Object.prototype.hasOwnProperty.call(input, 'value');
}

/** Get all the values of fields of a field name within a form.
 * This is useful to get the values of a Select in multi mode */
export const getFieldValuesByName = (formName: string, fieldName: string) => {
  const form = screen.getByTitle(formName);
  const fields = queryAllByName(form, fieldName);
  return fields.filter(isInput).map(field => field.value);
};
