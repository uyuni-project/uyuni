import * as React from "react";

import { cloneReactElement } from "components/utils";

import { SearchField } from "./SearchField";
import { TableDataHandler } from "./TableDataHandler";

type Props = {
  /** any type of data in an array, where each element is a row data */
  data: any[];

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: any) => any;

  /** a function that return a css class for each row */
  cssClassFunction?: (...args: any[]) => any;

  /** the React Object that contains the filter search field */
  searchField?: React.ReactComponentElement<typeof SearchField>;

  /** the initial number of how many row-per-page to show */
  initialItemsPerPage?: number;

  /** enables item selection */
  selectable: boolean;

  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: any[]) => void;

  /** the identifiers for selected items */
  selectedItems?: any[];

  /** The message which is shown when there are no rows to display */
  emptyText?: string;

  /** if data is loading */
  loading?: boolean;

  /** The message which is shown when the data is loading */
  loadingText?: string;

  /** Children node in the table */
  children: React.ReactNode;

  /** Other filter fields */
  additionalFilters?: React.ReactNode[];
};

export function CustomDataHandler(props: Props) {
  const { selectable, ...allProps } = props;
  return (
    <TableDataHandler {...allProps} selectable={() => selectable}>
      {({ currItems, criteria }) =>
        React.Children.toArray(props.children).map((child) => cloneReactElement(child, { data: currItems, criteria }))
      }
    </TableDataHandler>
  );
}

CustomDataHandler.defaultProps = {
  selectable: false,
};
