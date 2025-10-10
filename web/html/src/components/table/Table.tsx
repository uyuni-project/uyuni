import * as React from "react";
import { forwardRef, useImperativeHandle } from "react";

import { Button } from "components/buttons";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

import { Column } from "./Column";
import { SearchField } from "./SearchField";
import { TableDataHandler } from "./TableDataHandler";
import { useExpanded } from "./useExpanded";

type ArrayElement<A> = A extends readonly (infer T)[] ? T : never;

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
  data: any[] | string;

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: any) => any;

  /** the column key name of the initial sorted column */
  initialSortColumnKey?: string;

  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number;

  /** a function that return a css class for each row */
  cssClassFunction?: Function;

  /** Callback for search input, setting `onSearch` sets `searchField` to a simple search input if none is provided */
  onSearch?: (criteria: string) => void;

  /** the React Object that contains the filter search field */
  searchField?: React.ReactComponentElement<typeof SearchField>;

  /** the initial number of how many row-per-page to show */
  initialItemsPerPage?: number;

  /** Hide header and footer */
  hideHeaderFooter?: "header" | "footer" | "both";

  /** enables item selection. */
  selectable?: boolean | ((row: any) => boolean);

  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: any[]) => void;

  /** the identifiers for selected items */
  selectedItems?: any[];

  /** Allow items to be deleted or allow rows to be deleted on a case-by-case basis */
  deletable?: boolean | ((row: any) => boolean);

  /** The handler to call when an item is deleted. */
  onDelete?: (row: any) => void;

  /** Allow expanding table rows to reveal data held in the field `children` */
  expandable?: boolean;

  /** The message which is shown when there are no rows to display */
  emptyText?: string;

  /** Indicate whether the data is loading (only effective for tables using SimpleDataProvider) */
  loading?: boolean;

  /** The message which is shown when the data is loading */
  loadingText?: string;

  onLoad?: () => void;

  /** Children node in the table */
  children: React.ReactNode;

  /** Other filter fields */
  additionalFilters?: React.ReactNode[];

  /** Default search field */
  defaultSearchField?: string;

  /** Initial search query */
  initialSearch?: string;

  /** Title buttons to add next to the items per page selection */
  titleButtons?: React.ReactNode[];
};

function isColumn(input: any): input is React.ReactElement<React.ComponentProps<typeof Column>> {
  return input?.type === Column || input?.type?.displayName === "Column";
}

export type TableRef = {
  refresh: (...args: any[]) => any;
};

export const Table = forwardRef<TableRef, TableProps>((props, ref) => {
  const { ...allProps } = props;
  const columns = React.Children.toArray(props.children).filter(isColumn);
  const dataHandlerRef = React.useRef<TableDataHandler>(null);

  const expanded = useExpanded();

  useImperativeHandle(ref, () => ({
    refresh: () => {
      dataHandlerRef.current?.getData();
    },
  }));

  return (
    <TableDataHandler ref={dataHandlerRef} columns={columns} {...allProps}>
      {({ currItems, headers, handleSelect, selectedItems, criteria }) => {
        const selectableValue = DEPRECATED_unsafeEquals(props.selectable, null) ? false : props.selectable;

        const renderRow = (item: ArrayElement<typeof currItems>, index: number, nestingLevel: number) => {
          const cells: React.ReactNode[] = React.Children.toArray(props.children)
            .filter(isColumn)
            .map((column, index) =>
              React.cloneElement(column, {
                key: column.props.columnKey,
                data: item,
                criteria: criteria,
                columnClass: `${index === 0 ? `nesting-${nestingLevel}` : ""} ${column.props.columnClass ?? ""}`,
                nestingLevel,
              })
            );

          const isSelectable = typeof selectableValue === "boolean" ? () => selectableValue : selectableValue;
          if (selectableValue && isSelectable(item)) {
            const checkbox = (
              <Column
                key="check"
                columnKey="check"
                cell={
                  <input
                    type="checkbox"
                    checked={selectedItems.includes(props.identifier(item))}
                    onChange={(e) => handleSelect(props.identifier(item), e.target.checked)}
                  />
                }
              />
            );
            cells.unshift(checkbox);
          } else if (selectableValue && !isSelectable(item)) {
            const checkbox = <Column columnKey="check" cell={<input type="checkbox" disabled checked={false} />} />;
            cells.unshift(checkbox);
          }

          if (props.expandable) {
            const toggle = (
              <Column
                key="expandable"
                columnKey="expandable"
                onClick={() => expanded.toggle(props.identifier(item))}
                cell={() => {
                  const hasChildren = "children" in item && item.children.length > 0;
                  const isExpanded = expanded.has(props.identifier(item));
                  return (
                    <i
                      className={`expand-icon fa ${isExpanded ? "fa-chevron-down" : "fa-chevron-right"} ${
                        hasChildren ? "visible" : "invisible"
                      }`}
                    />
                  );
                }}
              />
            );
            cells.unshift(toggle);
          }

          if (props.deletable) {
            const deleteButton = (
              <Button
                className="btn-default btn-sm"
                title={t("Delete")}
                icon="fa-trash"
                handler={() => {
                  props.onDelete?.(item);
                }}
              />
            );
            const column = (
              <Column
                key="delete"
                columnKey="delete"
                cell={(row) => {
                  if (typeof props.deletable === "function") {
                    return props.deletable(row) ? deleteButton : null;
                  }
                  return deleteButton;
                }}
                data={item}
                criteria={criteria}
              />
            );
            cells.push(column);
          }

          const rowClass = props.cssClassFunction ? props.cssClassFunction(item, index) : "";
          let key = props.identifier(item);
          if (typeof key === "undefined") {
            Loggerhead.error(`Could not identify table row with identifier: ${props.identifier}`);
            key = index;
          }
          return (
            <React.Fragment key={key}>
              <tr className={rowClass}>{cells}</tr>
              {props.expandable &&
                "children" in item &&
                expanded.has(props.identifier(item)) &&
                item.children.map((childItem, childIndex) => renderRow(childItem, childIndex, nestingLevel + 1))}
            </React.Fragment>
          );
        };

        const rows = currItems.map((item, index) => renderRow(item, index, 0));

        return (
          <table className="table vertical-middle">
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
