import * as React from "react";
import Select from "react-select";

type SearchFieldProps = {
  /** The value that the user will enter to perform the filter on the criteriaField */
  criteria?: string;

  /** The field on which the user like to perform the filter */
  criteriaField?: any;

  /** This is the set of options that will be displayed */
  options?: any;

  /** This is the option that willbe displayed by default */
  defaultValue?: string | null;

  /** This feature allows us to clear the selected option from the bar */
  isClearable?: boolean;

  /** This feature allows us to search for an option by typing in the bar */
  isSearchable?: boolean;

  /** Place holder value to display when nothing has been input */
  placeholder?: string;

  /** function called when a search is performed.
   * This is usually passed by the search panel parent component.
   */
  onSearch?: (criteria?: string, criteriaField?: string) => void;

  /** filtering function */
  // This is manually used in TableDataHandler as an argument to SimpleDataProvider
  filter?: (datum: any, criteria?: string) => boolean;

  /** input field name */
  name?: string;
};

/** Text input search field */
export function SearchField(props: SearchFieldProps) {
  const [criteria, setCriteria] = React.useState("");

  const [criteriaField, setCriteriaField] = React.useState("");

  return (
    <React.Fragment>
      <Select
        className="col-md2"
        placeholder={props.placeholder}
        isClearable={props.isClearable}
        isSearchable={props.isSearchable}
        closeMenuOnSelect={true}
        autoFocus={true}
        options={props.options}
        defaultValue={props.defaultValue}
        value={props.criteriaField}
        name={props.name}
        noOptionsMessage={() => "No such option...."}
        onChange={(e) => {
          setCriteriaField(e.value);
          props.onSearch?.(criteria, criteriaField);
        }}
      />
      <input
        className="form-control table-input-search"
        type="text"
        value={props.criteria}
        onChange={(e) => {
          setCriteria(e.target.value);
          props.onSearch?.(criteria, criteriaField);
        }}
      />
    </React.Fragment>
  );
}
