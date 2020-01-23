// @flow
import * as React from 'react';

type ColumnProps = {
  /** key differenciating a column from its sibblings */
  key?: string,
  /** Content of the cell or function to compute it from the row data */
  cell: React.Node | (data: any, criteria?: string) => React.Node,
  /** Title of the row */
  header?: React.Node,
  /** CSS value for the column width */
  width?: string,
  /** key used to identify the column */
  columnKey?: string,
  /** Row comparison function. See sortBy functions in utils/functions.js */
  comparator?: (a: Object, b: Object, columnKey: string, sortDirection: number) => number,
  /** class name to use for the header cell */
  headerClass?: string,
  /** class name to use for the cell */
  columnClass?: string,
  /** the data associated with the row */
  data?: any,
  /** search criteria value */
  criteria?: string,
};

/** Represents a column in the table.
 * This component is also used internally to reprent each cell
 */
export function Column(props: ColumnProps) {
  let content = null;
  if (typeof props.cell === "function") {
    content = props.cell(props.data, props.criteria);
  } else {
    content = props.cell;
  }
  return <td className={props.columnClass}>{content}</td>;
}
Column.defaultProps = {
  header: undefined,
  comparator: undefined,
  columnClass: undefined,
  data: undefined,
  criteria: undefined,
};
