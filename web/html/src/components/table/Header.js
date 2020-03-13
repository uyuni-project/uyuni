// @flow
import * as React from 'react';

type HeaderProps = {
  /** key differenciating a header from its siblings */
  key?: string | number,
  /** CSS value for the column width */
  width?: string,
  /** class name to use for the cell */
  className?: string,
  /** Row comparison function. See sortBy functions in utils/functions.js */
  comparator?: (Object, Object, string, number) => number,
  /** 1 for ascending, -1 for descending, 0 for no change */
  sortDirection: number,
  /** Function called when the sort direction is changed. */
  onSortChange?: (columnKey?: string, sortDirection: number) => void,
  /** children nodes */
  children?: React.Node,
  /** identifier for the column */
  columnKey?: string,
};

/** Represents a header cell in the table.
 *  This component should only be used internally by Table.
 */
export function Header(props: HeaderProps) {
  const thStyle = props.width ? { width: props.width } : null;

  let thClass = props.className || '';

  if (props.comparator) {
    thClass += (thClass ? " " : "") + (props.sortDirection === 0 ? "" : (props.sortDirection > 0 ? "ascSort" : "descSort"));
    const newDirection = props.sortDirection === 0 ? 1 : props.sortDirection * -1;

    return (
      <th style={ thStyle } className={ thClass }>
        <a href="#" className="orderBy"
            onClick={() => props.onSortChange && props.onSortChange(props.columnKey, newDirection)}>
          {props.children}
        </a>
      </th>
    );
  }
  return <th style={ thStyle } className={ thClass }>{props.children}</th>;
}
Header.defaultProps = {
  width: undefined,
  columnClass: undefined,
  comparator: undefined,
  sortDirection: 0,
};
