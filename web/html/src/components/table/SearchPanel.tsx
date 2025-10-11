import * as React from "react";

import { cloneReactElement } from "components/utils";

import styles from "./SearchPanel.module.scss";

type SearchPanelProps = {
  /** number representing the number of the first displayed item */
  fromItem: number;

  /** number representing the number of the last displayed item */
  toItem: number;

  /** total number of filtered items */
  itemCount: number;

  /** number of selected items */
  selectedCount: number;

  /** flag indicating whether to show the number of selected items */
  selectable?: boolean;

  /** function called when the search value is changed. Takes the criteria as single parameter */
  onSearch: (criteria: string) => void;

  /** function called when the search field is changed. Takes the field as single parameter */
  onSearchField: (field: string) => void;

  /** function called when the clear button is clicked. This should reset the selection. */
  onClear: () => void;

  /** function called when the Select All button is clicked. Should set the selection */
  onSelectAll: () => void;

  /** Search criteria value */
  criteria?: string;

  /** Search field value */
  field?: string;

  /** Search field components */
  children?: React.ReactNode;

  /** Align search fields inline */
  searchPanelInline?: boolean
};

/** Panel containing the search fields for a table */
export function SearchPanel(props: SearchPanelProps) {
  return (
    <div className={`spacewalk-list-filter ${props.searchPanelInline ? styles.inlineSearchPanel : styles.searchPanel}`}>
      {React.Children.toArray(props.children).map((child) =>
        cloneReactElement(child, {
          criteria: props.criteria,
          field: props.field,
          onSearch: props.onSearch,
          onSearchField: props.onSearchField,
        })
      )}
    </div>
  );
}

SearchPanel.defaultProps = {
  selectable: false,
  selectedCount: 0,
};
