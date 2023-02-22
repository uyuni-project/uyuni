import * as React from "react";
import { forwardRef, useImperativeHandle } from "react";

import { Button } from "components/buttons";

import { Column } from "./Column";
import { SearchField } from "./SearchField";
import { TableDataHandler } from "./TableDataHandler";

type TableProps = {
  /**
   * Either an array of data items of any type where each element is a row data,
   * or a URI string to a resource endpoint that returns a paged list of data items.
   *
   * The data returned from the endpoint must be in paginated form as the following:
   * ```
   * {
   *    items: [..],
   *    total: TOTAL_ITEMS
   * }
   * ```
   *
   * See: utils/data-providers/paged-data-endpoint.js for async usage
   */
  data: Array<any> | string;

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: any) => any;

  /** the column key name of the initial sorted column */
  initialSortColumnKey?: string;

  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number;

  /** a function that return a css class for each row */
  cssClassFunction?: Function;

  /** the React Object that contains the filter search field */
  searchField?: React.ReactComponentElement<typeof SearchField>;

  /** the initial number of how many row-per-page to show */
  initialItemsPerPage?: number;

  /** enables item selection. */
  selectable: boolean | ((row: any) => boolean);

  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: Array<any>) => void;

  /** the identifiers for selected items */
  selectedItems?: Array<any>;

  /** Allow items to be deleted or allow rows to be deleted on a case-by-case basis */
  deletable?: boolean | ((row: any) => boolean);

  /** The handler to call when an item is deleted. */
  onDelete?: (row: any) => void;

  /** The message which is shown when there are no rows to display */
  emptyText?: string;

  /** Indicate whether the data is loading (only effective for tables using SimpleDataProvider) */
  loading?: boolean;

  /** The message which is shown when the data is loading */
  loadingText?: string;

  /** Children node in the table */
  children: React.ReactNode;

  /** Other filter fields */
  additionalFilters?: Array<React.ReactNode>;

  /** Default search field */
  defaultSearchField?: string;

  /** Initial search query */
  initialSearch?: string;

  /** Title buttons to add next to the items per page selection */
  titleButtons?: Array<React.ReactNode>;
};

function isColumn(input: any): input is React.ReactElement<React.ComponentProps<typeof Column>> {
  return input?.type === Column || input?.type?.displayName === "Column";
}

export type TableRef = {
  refresh: (...args: any[]) => any;
};

export const Table = forwardRef<TableRef, TableProps>((props, ref) => {
  const { ...allProps } = props;
  const columns = React.Children.toArray(props.children)
    .filter(isColumn)
    .map((child) => React.cloneElement(child));
  const dataHandlerRef = React.useRef<TableDataHandler>(null);

  useImperativeHandle(ref, () => ({
    refresh: () => {
      dataHandlerRef.current?.getData();
    },
  }));

  return (
    <TableDataHandler ref={dataHandlerRef} columns={columns} {...allProps}>
      {({ currItems, headers, handleSelect, selectable, selectedItems, deletable, criteria }) => {
        const rows = currItems.map((datum, index) => {
          const cells: React.ReactNode[] = React.Children.toArray(props.children)
            .filter(isColumn)
            .map((column) => React.cloneElement(column, { data: datum, criteria: criteria }));

          const isSelectable = typeof selectable === "boolean" ? () => selectable : selectable;
          if (selectable && isSelectable(datum)) {
            const checkbox = (
              <Column
                key="check"
                cell={
                  <input
                    type="checkbox"
                    checked={selectedItems.includes(props.identifier(datum))}
                    onChange={(e) => handleSelect(props.identifier(datum), e.target.checked)}
                  />
                }
              />
            );
            cells.unshift(checkbox);
          } else if (selectable && !isSelectable(datum)) {
            const checkbox = <Column key="check" cell={<input type="checkbox" disabled checked={false} />} />;
            cells.unshift(checkbox);
          }

          if (deletable) {
            const deleteButton = (
              <Button
                className="btn-default btn-sm"
                title={t("Delete")}
                icon="fa-trash"
                handler={() => {
                  props.onDelete?.(datum);
                }}
              />
            );
            const column = (
              <Column
                key="delete"
                cell={(row) => {
                  if (typeof deletable === "function") {
                    return deletable(row) ? deleteButton : null;
                  }
                  return deleteButton;
                }}
                data={datum}
                criteria={criteria}
              />
            );
            cells.push(column);
          }

          const rowClass = props.cssClassFunction ? props.cssClassFunction(datum, index) : "";
          const evenOddClass = index % 2 === 0 ? "list-row-odd" : "list-row-even";
          let key = props.identifier(datum);
          if (typeof key === "undefined") {
            Loggerhead.error(`Could not identify table row with identifier: ${props.identifier}`);
            key = index;
          }
          return (
            <tr className={rowClass + " " + evenOddClass} key={key}>
              {cells}
            </tr>
          );
        });

        return (
          <table className="table table-striped vertical-middle">
            <thead>
              <tr>{headers}</tr>
            </thead>
            <tbody>{rows}</tbody>
          </table>
        );
      }}
    </TableDataHandler>
  );
});

Table.defaultProps = {
  selectable: false,
};
