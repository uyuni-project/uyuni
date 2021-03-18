import { screen } from "@testing-library/react";

/** Get all the values of fields of a field name within a form.
  * This is usefull to get the values of a Select in multi mode */
export const getFieldValuesByName = (formName, fieldName) => {
  return [...screen.getByTitle(formName).elements]
    .filter(f => f.name === fieldName)
    .map(f => f.value);
}
