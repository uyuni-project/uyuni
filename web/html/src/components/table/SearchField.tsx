import * as React from "react";

type SearchFieldProps = {
  /** Search criteria value */
  criteria?: string;

  /** Place holder value to display when nothing has been input */
  placeholder?: string;

  /** function called when a search is performed.
   * This is usually passed by the search panel parent component.
   */
  onSearch?: (criteria: string) => void;

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
    return (
      <input
        className="form-control table-input-search"
        value={props.criteria || ""}
        placeholder={props.placeholder}
        type="text"
        onChange={(e) => props.onSearch?.(e.target.value)}
        name={props.name}
      />
    );
  }
}
