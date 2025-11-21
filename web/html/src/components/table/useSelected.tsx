import { useState } from "react";

import { Check } from "components/input";

import { Column, ColumnProps } from "./Column";
import styles from "./useSelected.module.scss";

type Node<T> = { children?: T[] };

/**
 * Defines what items will be selected when a item is checked
 */
export interface SelectionProvider<T extends Node<T>> {
  select(item: T): T[];

  unselect(item: T): T[];
}

/**
 * A {@link SelectionProvider} that selects/unselects the item and all its children.
 */
export class ChildrenSelectionProvider<T extends Node<T>> {
  public select(item: T): T[] {
    const result: T[] = [];

    result.push(item);
    if (item.children) {
      for (const child of item.children) {
        result.push(...this.select(child));
      }
    }

    return result;
  }

  public unselect(item: T): T[] {
    const result: T[] = [];

    result.push(item);
    if (item.children) {
      for (const child of item.children) {
        result.push(...this.unselect(child));
      }
    }

    return result;
  }
}

/**
 * Create a selectable table column
 *
 * @param identifier A function to identify table rows, for example `row => row.id`
 * @param getAllIdentifiers Async request to get all available table rows, including children, in order to select all items across all pages
 * @param initialSelection The initial selection state
 * @param onSelectionChange optional
 */
export const useSelected = <T extends Node<T>, I>(
  identifier: (item: T) => I,
  getAllIdentifiers: (() => Promise<I[]>) | undefined = undefined,
  selectionProvider: SelectionProvider<T> = new ChildrenSelectionProvider<T>(),
  initialSelection: I[] = [],
  onSelectionChange: ((newItems: T[], selected: boolean) => void) | undefined = undefined
) => {
  const [selected, setSelected] = useState(new Set<I>(initialSelection));
  // Fyi, we do NOT set `isAllSelected` even if you manually go and select everything on every page, we only set it from the async request
  const [isAllSelected, setIsAllSelected] = useState(false);

  const select = (item: T) => {
    setSelected((prev) => {
      const newSelected = new Set(prev);
      const idsToAdd = selectionProvider.select(item);

      idsToAdd.map(identifier).forEach((id) => newSelected.add(id));
      onSelectionChange?.(idsToAdd, true);

      return newSelected;
    });
  };

  const unselect = (item: T) => {
    setIsAllSelected(false);
    setSelected((prev) => {
      const newSelected = new Set(prev);
      const idsToRemove = selectionProvider.unselect(item);

      idsToRemove.map(identifier).forEach((id) => newSelected.delete(id));
      onSelectionChange?.(idsToRemove, false);

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
    if (isSelected) {
      return false;
    }

    let selectedChildren = 0;
    const countChildren = (node: T) => {
      if (node.children) {
        for (const child of node.children) {
          if (selected.has(identifier(child))) {
            selectedChildren++;
          }
          countChildren(child);
        }
      }
    };
    countChildren(item);

    return selectedChildren > 0;
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
