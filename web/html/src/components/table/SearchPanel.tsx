import { type ReactNode, Children } from "react";

import { cloneReactElement } from "components/utils";

import styles from "./SearchPanel.module.scss";

type SearchPanelProps = {
  /** function called when the search value is changed. Takes the criteria as single parameter */
  onSearch: (criteria: string) => void;

  /** function called when the search field is changed. Takes the field as single parameter */
  onSearchField: (field: string) => void;

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
      {Children.toArray(props.children).map((child) =>
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
