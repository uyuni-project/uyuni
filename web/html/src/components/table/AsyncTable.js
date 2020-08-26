// @flow
import * as React from 'react';
import { AsyncDataHandler } from './AsyncDataHandler';
import { SearchField } from './SearchField';
import { Column } from './Column';

type AsyncTableProps = {
  /** Resource URI */
  resource: string,
  /** Function extracting the unique key of the row from the data object */
  identifier: Function,
  /** the column key name of the initial sorted column */
  initialSortColumnKey?: string,
  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number,
  /** a function that return a css class for each row */
  cssClassFunction?: Function,
  /** the React Object that contains the filter search field */
  searchField?: React.Element<typeof SearchField>,
  /** the initial number of how many row-per-page to show */
  initialItemsPerPage?: number,
  /** enables item selection */
  selectable: boolean,
  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: Array<any>) => void,
  /** the identifiers for selected items */
  selectedItems?: Array<any>,
  /** The message which is shown when there are no rows to display */
  emptyText?: string,
  /** The message which is shown when the data is loading */
  loadingText?: string,
  /** Children node in the table */
  children: React.Node,
  /** Other filter fields */
  additionalFilters?: Array<React.Node>,
};

export function AsyncTable(props: AsyncTableProps): React.Node {
  const {...allProps} = props;
  const columns = React.Children.toArray(props.children)
    .filter((child) => child.type === Column || child.type.displayName === "Column")
    .map((child) => React.cloneElement(child));
  return (
    <AsyncDataHandler
      columns={columns}
      {...allProps}
    >
    {
      ({currItems, headers, handleSelect, selectable, selectedItems, criteria}) => {
        const rows = currItems.map((datum, index) => {
            const cells = React.Children.toArray(props.children)
              .filter((child) => child.type === Column || child.type.displayName === "Column")
              .map((column) => React.cloneElement(column, {data: datum, criteria: criteria})
            );

            if (selectable) {
              const checkbox = <Column key="check" cell={
                <input type="checkbox"
                    checked={selectedItems.includes(props.identifier(datum))}
                    onChange={(e) => handleSelect(props.identifier(datum), e.target.checked)}
                />
              }/>;
              cells.unshift(checkbox);
            }

            const rowClass = props.cssClassFunction ? props.cssClassFunction(datum, index) : "";
            const evenOddClass = (index % 2) === 0 ? "list-row-even" : "list-row-odd";
            return (
              <tr className={rowClass + " " + evenOddClass}
                key={props.identifier(datum)}
              >
                {cells}
              </tr>
            );
        });

        return (
          <table className="table table-striped vertical-middle">
            <thead>
              <tr>{headers}</tr>
            </thead>
            <tbody>
              {rows}
            </tbody>
          </table>
        );
      }
    }
    </AsyncDataHandler>
  );
};
AsyncTable.defaultProps = {
  selectable: false,
};
