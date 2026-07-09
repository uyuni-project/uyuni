export type SelectionSummary = {
  selected: number;
  total: number;
};

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

export function getSelectionSummary(item: ProductLike, selectedItems: ProductLike[]): SelectionSummary {
  const selectedIds = new Set(selectedItems.map((item) => item.identifier));

  return computeSelectionSummary(item, selectedIds);
}

function computeSelectionSummary(item: ProductLike, selectedIds: Set<string>): SelectionSummary {
  const children = (item.extensions ?? []).filter(hasVisibleCheckbox);

  if (children.length === 0) {
    const isSelected = item.status === PRODUCT_STATUS.installed || selectedIds.has(item.identifier);

    return {
      selected: isSelected ? 1 : 0,
      total: 1,
    };
  }

  return children.reduce<SelectionSummary>(
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
