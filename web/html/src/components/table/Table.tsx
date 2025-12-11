import {
  type ComponentProps,
  type ReactComponentElement,
  type ReactElement,
  type ReactNode,
  Children,
  cloneElement,
  forwardRef,
  Fragment,
  useEffect,
  useImperativeHandle,
  useRef,
} from "react";

import { Button } from "components/buttons";

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

  /** Extracting the unique key of the row from the data object while filtering */
  controlledExpandedKeys?: Set<any>;

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: any) => any;

  /** the column key name of the initial sorted column */
  initialSortColumnKey?: string;

  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number;

  /** a function that return a css class for each row */
  cssClassFunction?: (...args: any[]) => any;

  /** Callback for search input, setting `onSearch` sets `searchField` to a simple search input if none is provided */
  onSearch?: (criteria: string) => void;

  /** the React Object that contains the filter search field */
  searchField?: ReactComponentElement<typeof SearchField>;

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

  /** Enables expandable rows */
  expandableOpen?: boolean;

  /** Automatically expands all expandable rows when data is loaded. 
  Useful for cases like search results where all matching rows should be visible by default.*/
  onDataLoaded?: (currItems: any[], info: TableLoadInfo) => void;

  /** The message which is shown when there are no rows to display */
  emptyText?: string;

  /** Indicate whether the data is loading (only effective for tables using SimpleDataProvider) */
  loading?: boolean;

  /** The message which is shown when the data is loading */
  loadingText?: string;

  onLoad?: () => void;

  /** Children node in the table */
  children: ReactNode;

  /** Other filter fields */
  additionalFilters?: ReactNode[];

  /** Default search field */
  defaultSearchField?: string;

  /** Initial search query */
  initialSearch?: string;

  /** Title buttons to add next to the items per page selection */
  titleButtons?: ReactNode[];

  /** Make header sticky */
  stickyHeader?: boolean;

  /** Add class to table */
  tableClass?: string;
};

function isColumn(input: any): input is ReactElement<ComponentProps<typeof Column>> {
  return input?.type === Column || input?.type?.displayName === "Column";
}

export type TableRef = {
  refresh: (...args: any[]) => any;
};

export const Table = forwardRef<TableRef, TableProps>((props, ref) => {
  const { ...allProps } = props;
  const columns = Children.toArray(props.children).filter(isColumn);
  const dataHandlerRef = useRef<TableDataHandler>(null);

  const expanded = useExpanded();

  useEffect(() => {
    if (props.controlledExpandedKeys) {
      expanded.set(Array.from(props.controlledExpandedKeys));
    }
  }, [props.controlledExpandedKeys]);

  useImperativeHandle(ref, () => ({
    refresh: () => {
      dataHandlerRef.current?.getData();
    },
    clearExpanded: () => {
      expanded.clear();
    },
  }));

  const handleDataLoaded = (currItems: any[], info: TableLoadInfo) => {
    // Auto-expand here
    if (props.expandable && props.expandableOpen) {
      const allKeys = currItems.map((item) => props.identifier(item));
      expanded.set(allKeys);
    }

    // Forward to parent if they provided a callback
    props.onDataLoaded?.(currItems, info);
  };

  return (
    <TableDataHandler ref={dataHandlerRef} columns={columns} {...allProps} onDataLoaded={handleDataLoaded}>
      {({ currItems, headers, handleSelect, selectedItems, criteria, headerHeight }) => {
        const selectableValue = props.selectable === null ? false : props.selectable;

        const renderRow = (item: ArrayElement<typeof currItems>, index: number, nestingLevel: number) => {
          const cells: ReactNode[] = Children.toArray(props.children)
            .filter(isColumn)
            .map((column, index) =>
              cloneElement(column, {
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
            <Fragment key={key}>
              <tr className={rowClass}>{cells}</tr>
              {props.expandable &&
                "children" in item &&
                expanded.has(props.identifier(item)) &&
                item.children.map((childItem, childIndex) => renderRow(childItem, childIndex, nestingLevel + 1))}
            </Fragment>
          );
        };

        const rows = currItems.map((item, index) => renderRow(item, index, 0));
        return (
          <table className={`table vertical-middle ${props.tableClass || ""}`}>
            <thead
              className={props.stickyHeader ? "position-sticky" : ""}
              style={props.stickyHeader ? { top: headerHeight } : null}
            >
              <tr>{headers}</tr>
            </thead>
            <tbody>{rows}</tbody>
          </table>
        );
      }}
    </TableDataHandler>
  );
});
