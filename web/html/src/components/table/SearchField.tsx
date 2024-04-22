import * as React from "react";

import { Form } from "components/input/Form";
import { Select } from "components/input/Select";

type SearchFieldOption = {
  label: string;
  value: string;
};

type SearchFieldProps = {
  /** The value that the user will enter to perform the filter on the criteriaField */
  criteria?: string;

  /** The field on which the user like to perform the filter */
  field?: string;

  /** This is the set of options that will be displayed */
  options?: SearchFieldOption[];

  /** Place holder value to display when nothing has been input */
  placeholder?: string;

  /** function called when the search value is changed. Triggers a new search.
   * This is usually passed by the search panel parent component.
   */
  onSearch?: (criteria: string) => void;

  /** function called when the search field is changed. Triggers a new search.
   * This is usually passed by the search panel parent component.
   */
  onSearchField?: (field: string) => void;

  /** filtering function */
  // This is manually used in TableDataHandler as an argument to SimpleDataProvider
  filter?: (datum: any, criteria?: string) => boolean;

  /** input field name */
  name?: string;
};

/** Text input search field */
export function SearchField(props: SearchFieldProps) {
  // Dummy model and onChange to reuse the Select component as it requires a Form
  let model = {};
  const onChange = () => {};

  return (
    <Form model={model} onChange={onChange} title={t("Filter")}>
      {props.options != null && (
        <Select
          name="filter"
          placeholder={t("Select a filter")}
          defaultValue={props.field}
          options={props.options}
          onChange={(name: string | undefined, value: string) => {
            console.log("onChange", name, value);
            props.onSearchField?.(value);
          }}
        />
      )}
      <div className="form-group">
        <input
          className="form-control table-input-search"
          data-testid="default-table-search"
          value={props.criteria || ""}
          placeholder={props.placeholder}
          type="text"
          onChange={(e) => props.onSearch?.(e.target.value)}
          name={props.name}
        />
      </div>
    </Form>
  );
}
