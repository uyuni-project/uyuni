import * as React from "react";
import Select from "react-select";

type SearchFieldProps = {
  /** The value that the user will enter to perform the filter on the criteriaField */
  criteria?: string;

  /** The field on which the user like to perform the filter */
  criteriaField?: any;

  /** This is the option that willbe displayed by default */
  defaultValue?: string | null;

  /** This is the set of options that will be displayed */
  options?: any;

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
export class SearchField extends React.Component<SearchFieldProps> {
  render() {
    const props = this.props;

    const [criteria, setCriteria] = React.useState([]);

    const [criteriaField, setCriteriaField] = React.useState([]);

    return (
      <React.Fragment>
        <Select
          className="basic-single"
          classNamePrefix="select"
          placeholder={props.placeholder}
          isClearable={props.isClearable}
          isSearchable={props.isSearchable}
          closeMenuOnSelect={true}
          autoFocus={true}
          defaultValue={props.defaultValue}
          options={props.options}
          noOptionsMessage={() => "No such option...."}
          onChange={(e) => {
            const criteriaField = e.newValue;
            setCriteriaField(criteriaField);
            props.onSearch?.(criteria, criteriaField);
          }}
        />
        <input
          className="form-control table-input-search"
          type="text"
          value={props.criteria || ""}
          onChange={(e) => {
            const criteria = e.target.value;
            setCriteria(criteria);
            props.onSearch?.(criteria, criteriaField);
          }}
        />
        <input type="submit" value="Filter" onClick={(e) => props.onSearch?.(criteria, criteriaField)} />
      </React.Fragment>
    );
  }
}
