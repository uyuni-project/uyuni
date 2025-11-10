import * as React from "react";

import { Comparator } from "utils/data-providers";

export type ColumnProps = {
  /** key differenciating a column from its sibblings */
  columnKey: string;

  /** Content of the cell or function to compute it from the row data */
  cell?: React.ReactNode | ((data: any, criteria?: string, nestingLevel?: number) => React.ReactNode);

  /** Title of the row, prefer `string` where possible for consistency */
  header?: string | React.ReactNode;

  /** CSS value for the column width */
  width?: string;

  /** Row comparison function. See sortBy functions in utils/functions.js */
  comparator?: Comparator;

  /**
   * If the column should be sortable
   * If a comparator is specified, this defaults to true.
   * If set to true without specifying a comparator, alphabetical comparison will be used
   * by default.
   * */
  sortable?: boolean;

  /** class name to use for the header cell */
  headerClass?: string;

  /** class name to use for the cell */
  columnClass?: string;

  /** the data associated with the row */
  data?: any;

  /** search criteria value */
  criteria?: string;

  /** On click for columns that behave as raw buttons */
  onClick?: (data?: any) => void;

  /** Disable `onClick` */
  disabled?: boolean;

  nestingLevel?: number;
};

/**
 * Represents a column in the table.
 * This component is also used internally to reprent each cell
 */
export function Column(props: ColumnProps) {
  let content: React.ReactNode = null;
  if (typeof props.cell === "function") {
    content = props.cell(props.data, props.criteria, props.nestingLevel);
  } else {
    content = props.cell;
  }
  return (
    <td
      className={props.columnClass}
      key={props.columnKey}
      role={props.onClick ? "button" : undefined}
      onClick={props.onClick && !props.disabled ? () => props.onClick?.(props.data) : undefined}
    >
      {content}
    </td>
  );
}
