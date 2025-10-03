import * as React from "react";
import { useState } from "react";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

import { SearchField } from "./SearchField";
import { Table, TableRef } from "./Table";

export type HierarchicalRow = {
  id: string | number;
  parentId?: string | number | null;
  level?: number;
  expanded?: boolean;
  isLeaf?: boolean;
  [key: string]: any;
};

type HierarchicalTableProps = {
  /** Optional class to add to the table container */
  className?: string;

  /**
   * Array of data items where each element has a unique id and optional parentId
   */
  data: HierarchicalRow[];

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: HierarchicalRow) => string | number;

  /** The search field to show on the table  */
  searchField?: React.ReactComponentElement<typeof SearchField>;

  /** Other filter fields */
  additionalFilters?: React.ReactNode[];

  /** Function to determine which column has expand/collapse controls */
  expandColumnKey?: string;

  /** Initial expanded state of all nodes (true = all expanded, false = all collapsed) */
  initiallyExpanded?: boolean;

  /** The column key name of the initial sorted column */
  initialSortColumnKey?: string;

  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number;

  /** a function that return a css class for each row */
  cssClassFunction?: Function;

  /** enables item selection. */
  selectable?: boolean | ((row: any) => boolean);

  /** the handler to call when the table selection is updated */
  onSelect?: (items: any[]) => void;

  /** the identifiers for selected items */
  selectedItems?: any[];

  /** Indent size in pixels per level */
  indentSize?: number;

  /** Children node in the table (Column components) */
  children: React.ReactNode;
};

export const HierarchicalTable = React.forwardRef<TableRef, HierarchicalTableProps>((props, ref) => {
  const {
    className,
    data,
    identifier,
    expandColumnKey,
    initiallyExpanded = false,
    indentSize = 20,
    children,
    ...tableProps
  } = props;

  const [expandedRows, setExpandedRows] = useState<Record<string | number, boolean>>(() => {
    const initialState: Record<string | number, boolean> = {};
    data.forEach((row) => {
      initialState[identifier(row)] = initiallyExpanded;
    });
    return initialState;
  });

  const buildTreeStructure = (items: HierarchicalRow[]) => {
    // Create a map of all items by ID
    const itemMap: Record<string | number, HierarchicalRow> = {};

    // First pass: map all items by their IDs and initialize level to 0
    items.forEach((item) => {
      const itemId = identifier(item);
      itemMap[itemId] = { ...item, level: 0, isLeaf: true };
    });
    // Second pass: identify parent-child relationships and mark non-leaves
    items.forEach((item) => {
      const parentId = item.parentId;
      if (!DEPRECATED_unsafeEquals(parentId, null) && itemMap[parentId]) {
        itemMap[parentId].isLeaf = false;
      }
    });
    // Third pass: calculate the level of each node
    const calculateLevel = (id: string | number, visited = new Set<string | number>()): number => {
      // Prevent circular references
      if (visited.has(id)) return 0;
      visited.add(id);
      const item = itemMap[id];
      if (!item || !item.parentId || !itemMap[item.parentId]) {
        return 0;
      }
      item.level = 1 + calculateLevel(item.parentId, visited);
      return item.level;
    };

    Object.keys(itemMap).forEach((id) => {
      if (itemMap[id] && itemMap[id].level === 0) {
        calculateLevel(id);
      }
    });

    return Object.values(itemMap);
  };

  const getVisibleRows = (treeData: HierarchicalRow[]) => {
    // Find root nodes (nodes without parents or with parents that don't exist in our data)
    const rootNodes = treeData.filter((row) => {
      return !row.parentId || !treeData.some((parent) => identifier(parent) === row.parentId);
    });

    const visibleRows: HierarchicalRow[] = [];

    const addVisibleDescendants = (node: HierarchicalRow) => {
      visibleRows.push(node);
      // Only process children if this node is expanded
      if (expandedRows[identifier(node)]) {
        // Find all direct children of this node
        const nodeId = identifier(node);
        const childNodes = treeData.filter((row) => row.parentId === nodeId);
        // Process each child recursively
        childNodes.forEach((child) => addVisibleDescendants(child));
      }
    };
    // Start with root nodes and build the visible tree
    rootNodes.forEach((root) => addVisibleDescendants(root));
    return visibleRows;
  };

  const toggleRowExpanded = (rowId: string | number) => {
    setExpandedRows((prev) => ({
      ...prev,
      [rowId]: !prev[rowId],
    }));
  };

  const treeData = buildTreeStructure(data);
  const visibleRows = getVisibleRows(treeData);

  const renderCellContent = (row: HierarchicalRow, child: React.ReactElement) => {
    // Get the cell content (either from custom renderer or from data property)
    const cellContent = child.props.cell ? child.props.cell(row) : row[child.props.columnKey || ""];
    // Apply special styling for non-leaf or indented nodes
    const level = row.level || 0;
    if (!row.isLeaf || level > 0) {
      return <span style={{ paddingLeft: row.isLeaf ? `${level * indentSize}px` : "0" }}>{cellContent}</span>;
    }
    // Return plain content for root-level leaf nodes
    return cellContent;
  };

  const isColumnElement = (element: React.ReactNode): element is React.ReactElement => {
    if (!React.isValidElement(element)) return false;
    return element.props && ("columnKey" in element.props || "header" in element.props || "cell" in element.props);
  };

  const enhancedChildren = React.Children.map(children, (child) => {
    if (!isColumnElement(child)) return child;
    if (child.props.columnKey === expandColumnKey) {
      const cellRenderer = (row: any) => {
        const rowId = identifier(row);
        const isExpanded = expandedRows[rowId];
        const level = row.level || 0;
        const indent = level * indentSize;

        return (
          <div className="expandable-entry">
            <div style={{ paddingLeft: `${indent}px` }}>
              {!row.isLeaf && (
                <i
                  className={`fa ${isExpanded ? "fa-angle-down" : "fa-angle-right"} fa-1-5x pointer product-hover`}
                  onClick={() => toggleRowExpanded(rowId)}
                />
              )}
              {renderCellContent(row, child)}
            </div>
          </div>
        );
      };
      return React.cloneElement(child, {
        ...child.props,
        cell: cellRenderer,
      });
    }
    return child;
  });

  const enhancedCssClassFunction = (row, index) => {
    let cssClass = props.cssClassFunction ? props.cssClassFunction(row, index) : "";
    cssClass += ` tree-level-${row.level || 0}`;
    if (!row.isLeaf) {
      cssClass += " tree-parent-node";
    }
    if (row.isLeaf) {
      cssClass += " tree-leaf-node";
    }
    return cssClass;
  };

  return (
    <div className={`hierarchical-table${className ? " " + className : ""}`}>
      <Table
        ref={ref}
        data={visibleRows}
        identifier={identifier}
        cssClassFunction={enhancedCssClassFunction}
        {...tableProps}
      >
        {enhancedChildren}
      </Table>
    </div>
  );
});

HierarchicalTable.defaultProps = {
  initiallyExpanded: false,
  indentSize: 20,
};
