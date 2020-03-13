// @flow
import * as React from 'react';

type SearchFieldProps = {
  /** Search criteria value */
  criteria?: string,
  /** Place holder value to display when nothing has been input */
  placeholder: string,
  /** function called when a search is performed.
   * This is usually passed by the search panel parent component.
   */
  onSearch?: (string) => void,
  /** filtering function */
  filter: (datum: any, criteria?: string) => boolean,
  /** input field name */
  name?: string,
}

/** Text input search field */
export function SearchField(props: SearchFieldProps) {
  return (
    <input className="form-control table-input-search with-bottom-margin"
      value={props.criteria}
      placeholder={props.placeholder}
      type="text"
      onChange={(e) => props.onSearch && props.onSearch(e.target.value)}
      name={props.name}
    />
  );
};

