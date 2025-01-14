import * as React from "react";

import { Button } from "components/buttons";
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
};

/** Panel containing the search fields for a table */
export function SearchPanel(props: SearchPanelProps) {
  return (
    <div className={`spacewalk-list-filter ${styles.searchPanel}`}>
      {React.Children.toArray(props.children).map((child) =>
        cloneReactElement(child, {
          criteria: props.criteria,
          field: props.field,
          onSearch: props.onSearch,
          onSearchField: props.onSearchField,
        })
      )}
      <div className={styles.pagination}>
        <span>
          {t("Items {from} - {to} of {total}", { from: props.fromItem, to: props.toItem, total: props.itemCount })}
          &nbsp;&nbsp;
        </span>
        {props.selectable && props.selectedCount > 0 && (
          <span>
            {t("({selectedCount} selected)", { selectedCount: props.selectedCount })}&nbsp;
            <Button className="btn-tertiary" handler={props.onClear} text={t("Clear")} />
            &nbsp;/&nbsp;
          </span>
        )}
        {props.selectable && <Button className="btn-tertiary" handler={props.onSelectAll} text={t("Select All")} />}
      </div>
    </div>
  );
}

SearchPanel.defaultProps = {
  selectable: false,
  selectedCount: 0,
};
