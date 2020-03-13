// @flow
import * as React from 'react';

type SearchPanelProps = {
  /** number representing the number of the first displayed item */
  fromItem: number,
  /** number representing the number of the last displayed item */
  toItem: number,
  /** total number of filtered items */
  itemCount: number,
  /** number of selected items */
  selectedCount: number,
  /** flag indicating whether to show the number of selected items */
  selectable?: boolean,
  /** function called when a search is performed. Takes the criteria as single parameter */
  onSearch: (string) => void,
  /** function called when the clear button is clicked. This should reset the selection. */
  onClear: () => void,
  /** function called when the Select All button is clicked. Should set the selection */
  onSelectAll: () => void,
  /** Search criteria value */
  criteria?: string,
  /** Search field components */
  children?: React.Node,
}

/** Panel containing the search fields for a table */
export function SearchPanel(props: SearchPanelProps) {
  return (
    <div className="spacewalk-list-filter table-search-wrapper">
      {
        React.Children.toArray(props.children)
          .filter(child => child != null)
          .map(
            (child) => React.cloneElement(child, { criteria: props.criteria, onSearch: props.onSearch }
          ))
      }
      <div className="d-inline-block">
        <span>{t("Items {0} - {1} of {2}", props.fromItem, props.toItem, props.itemCount)}&nbsp;&nbsp;</span>
        { props.selectable && props.selectedCount > 0 &&
            <span>
                {t("({0} selected)", props.selectedCount)}&nbsp;
                <a href="#"onClick={props.onClear}>{t("Clear")}</a>
                &nbsp;/&nbsp;
            </span>
        }
        { props.selectable &&
            <a href="#" onClick={props.onSelectAll}>{t("Select All")}</a>
        }
      </div>
    </div>
  );
};

SearchPanel.defaultProps = {
  selectable: false,
  selectedCount: 0,
}
