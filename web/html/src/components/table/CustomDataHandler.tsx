import * as React from "react";
import { TableDataHandler } from "./TableDataHandler";
import { SearchField } from "./SearchField";
import { cloneReactElement } from "components/utils";

type Props = {
  /** any type of data in an array, where each element is a row data */
  data: Array<any>;

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: any) => any;

  /** a function that return a css class for each row */
  cssClassFunction?: Function;

  /** the React Object that contains the filter search field */
  searchField?: React.ReactComponentElement<typeof SearchField>;

  /** the initial number of how many row-per-page to show */
  initialItemsPerPage?: number;

  /** enables item selection */
  selectable: boolean;

  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: Array<any>) => void;

  /** the identifiers for selected items */
  selectedItems?: Array<any>;

  /** The message which is shown when there are no rows to display */
  emptyText?: string;

  /** if data is loading */
  loading?: boolean;

  /** The message which is shown when the data is loading */
  loadingText?: string;

  /** Children node in the table */
  children: React.ReactNode;

  /** Other filter fields */
  additionalFilters?: Array<React.ReactNode>;
};

export function CustomDataHandler(props: Props) {
  const { ...allProps } = props;
  return (
    <TableDataHandler {...allProps}>
      {({ currItems, headers, handleSelect, selectable, selectedItems, criteria }) =>
        React.Children.toArray(props.children).map((child) => cloneReactElement(child, { data: currItems, criteria }))
      }
    </TableDataHandler>
  );
}
CustomDataHandler.defaultProps = {
  selectable: false,
};
