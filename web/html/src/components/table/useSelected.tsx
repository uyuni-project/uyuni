import { useState } from "react";

import { Check } from "components/input";

import { Column, ColumnProps } from "./Column";
import styles from "./useSelected.module.scss";

/**
 * Create a selectable table column
 *
 * @param identifier A function to identify table rows, for example `row => row.id`
 * @param getAllIdentifiers Async request to get all available table rows, including children, in order to select all items across all pages
 */
export const useSelected = <T extends { children?: T[] }, I>(
  identifier: (item: T) => I,
  getAllIdentifiers?: () => Promise<I[]>
) => {
  const [selected, setSelected] = useState(new Set<I>());
  // Fyi, we do NOT set `isAllSelected` even if you manually go and select everything on every page, we only set it from the async request
  const [isAllSelected, setIsAllSelected] = useState(false);

  const selectRecursive = (item: T, targetSet: Set<I>) => {
    targetSet.add(identifier(item));
    if (item.children) {
      for (const child of item.children) {
        selectRecursive(child, targetSet);
      }
    }
  };

  const unselectRecursive = (item: T, targetSet: Set<I>) => {
    targetSet.delete(identifier(item));
    if (item.children) {
      for (const child of item.children) {
        unselectRecursive(child, targetSet);
      }
    }
  };

  const select = (item: T) => {
    setSelected((prev) => {
      const newSelected = new Set(prev);
      selectRecursive(item, newSelected);
      return newSelected;
    });
  };

  const unselect = (item: T) => {
    setIsAllSelected(false);
    setSelected((prev) => {
      const newSelected = new Set(prev);
      unselectRecursive(item, newSelected);
      return newSelected;
    });
  };

  const isSelected = (item: T): boolean => {
    return selected.has(identifier(item));
  };

  const isIndeterminate = (item: T): boolean => {
    if (!item.children || item.children.length === 0) {
      return false;
    }
    const isSelected = selected.has(identifier(item));

    let selectedChildren = 0;
    let totalChildren = 0;
    const countChildren = (node: T) => {
      if (node.children) {
        for (const child of node.children) {
          totalChildren++;
          if (selected.has(identifier(child))) {
            selectedChildren++;
          }
          countChildren(child);
        }
      }
    };
    countChildren(item);

    return isSelected ? selectedChildren !== totalChildren : selectedChildren > 0;
  };

  const toggle = (item: T) => {
    if (isSelected(item)) {
      unselect(item);
    } else {
      select(item);
    }
  };

  const toggleSelectAll = async () => {
    if (!getAllIdentifiers) {
      throw new RangeError("Missing getAllIdentifiers in useSelected");
    }
    if (isAllSelected) {
      setSelected(new Set());
      setIsAllSelected(false);
    } else {
      const allIdentifiers = await getAllIdentifiers();
      setSelected(new Set(allIdentifiers));
      setIsAllSelected(true);
    }
  };

  const clear = () => {
    setSelected(new Set());
  };

  const SelectionColumn = (props: ColumnProps) => (
    <Column
      onClick={(item) => toggle(item)}
      cell={(item) => <Check indeterminate={isIndeterminate(item)} checked={isSelected(item)} />}
      {...props}
    />
  );

  const Header = (props: { children?: React.ReactNode }) => (
    <>
      <Check
        checked={isAllSelected}
        onChange={() => toggleSelectAll()}
        className={props.children ? styles.check : undefined}
      />
      {props.children}
    </>
  );

  // Table.tsx needs to know this is a real column for `isColumn`
  SelectionColumn.displayName = "Column";

  return {
    /** Render this selection as a `<Column />` */
    Column: SelectionColumn,
    Header,
    /** Recursively select an item */
    select,
    /** Recursively unselect an item */
    unselect,
    /** Recursively toggle an item */
    toggle,
    /** Is a given item currently selected */
    isSelected,
    /** Does a given item have mixed children, some selected, some not selected */
    isIndeterminate,
    /** Select or unselect all items across all pages */
    toggleSelectAll,
    /** Are all items across all pages selected */
    isAllSelected,
    /** Set selected values, overwriting all previous values */
    setSelected(iterable?: Iterable<I>) {
      setSelected(new Set(iterable));
    },
    /** Add selected values to the existing selection, keeping previous values */
    addSelected(iterable?: Iterable<I>) {
      setSelected((prev) => new Set([...prev, ...(iterable || [])]));
    },
    /** Get a list of currently selected items across all nesting levels */
    getSelected() {
      return Array.from(selected);
    },
    /** Clear selection */
    clear,
  };
};

export type UseSelected = ReturnType<typeof useSelected>;
