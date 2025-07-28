import { useState } from "react";

import { Column, ColumnProps } from "./Column";

export const useSelected = <T extends { children?: T[] }, I>(identifier: (item: T) => I) => {
  const [selected, setSelected] = useState(new Set<I>());

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

  const clear = () => {
    setSelected(new Set());
  };

  const SelectionColumn = (props: ColumnProps) => (
    <Column
      onClick={(item) => toggle(item)}
      cell={(
        item // TODO: Use the new Checkbox here once that PR is merged
      ) => (
        <div style={isIndeterminate(item) ? { background: "red" } : undefined}>
          <input
            type="checkbox"
            checked={isSelected(item)}
            // indeterminate={props.selected.isIndeterminate(item)}
            readOnly
          />
        </div>
      )}
      {...props}
      // The props below this line may be overwritten by the spread props
      width="30px"
      columnClass="text-center"
      headerClass="text-center"
    />
  );

  // Table.tsx needs to know this is a real column for `isColumn`
  SelectionColumn.displayName = "Column";

  return {
    /** Render this selection as a <Column /> */
    Column: SelectionColumn,
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
