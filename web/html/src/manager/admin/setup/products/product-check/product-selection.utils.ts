export type ProductSelectionState = "checked" | "unchecked" | "partially";

export type ProductStatus = "INSTALLED" | "AVAILABLE" | "UNAVAILABLE";

export type ProductLike = {
  identifier: string;
  status?: ProductStatus;
  extensions?: ProductLike[];
};

export const PRODUCT_STATUS = {
  installed: "INSTALLED",
  available: "AVAILABLE",
  unavailable: "UNAVAILABLE",
} as const satisfies Record<string, ProductStatus>;

const hasVisibleCheckbox = (item: ProductLike) =>
  item.status === PRODUCT_STATUS.available || item.status === PRODUCT_STATUS.installed;

function computeState(item: ProductLike, selectedIds: Set<string>): ProductSelectionState {
  const { status, extensions = [] } = item;
  const isSelected = status === PRODUCT_STATUS.installed || selectedIds.has(item.identifier);
  const selectableChildren = extensions.filter(hasVisibleCheckbox);

  if (selectableChildren.length === 0) {
    return isSelected ? "checked" : "unchecked";
  }

  const childStates = selectableChildren.map((child) => computeState(child, selectedIds));

  if (isSelected && childStates.every((state) => state === "checked")) {
    return "checked";
  }

  if (!isSelected && childStates.every((state) => state === "unchecked")) {
    return "unchecked";
  }

  return "partially";
}

export function getProductSelectionState(item: ProductLike, selectedItems: ProductLike[]): ProductSelectionState {
  const selectedIds = new Set(selectedItems.map((selectedItem) => selectedItem.identifier));

  return computeState(item, selectedIds);
}

export function getSelectionSummary(item: ProductLike, selectedItems: ProductLike[]) {
  const selectedIds = new Set(selectedItems.map((i) => i.identifier));

  return computeSelectionSummary(item, selectedIds);
}

function computeSelectionSummary(item, selectedIds) {
  const children = (item.extensions ?? []).filter(hasVisibleCheckbox);

  if (children.length === 0) {
    const isSelected = item.status === PRODUCT_STATUS.installed || selectedIds.has(item.identifier);

    return {
      selected: isSelected ? 1 : 0,
      total: 1,
    };
  }

  return children.reduce(
    (summary, child) => {
      const childSummary = computeSelectionSummary(child, selectedIds);

      return {
        selected: summary.selected + childSummary.selected,
        total: summary.total + childSummary.total,
      };
    },
    {
      selected: 0,
      total: 0,
    }
  );
}
