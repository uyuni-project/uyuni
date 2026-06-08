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

const isProductSelected = (item: ProductLike, selectedItems: ProductLike[]) =>
  selectedItems.some((selectedItem) => selectedItem.identifier === item.identifier);

export function getProductSelectionState(item: ProductLike, selectedItems: ProductLike[]): ProductSelectionState {
  const { status, extensions = [] } = item;
  const isSelected = status === PRODUCT_STATUS.installed || isProductSelected(item, selectedItems);
  const selectableChildren = extensions.filter(hasVisibleCheckbox);

  if (selectableChildren.length === 0) {
    return isSelected ? "checked" : "unchecked";
  }

  const childStates = selectableChildren.map((child) => getProductSelectionState(child, selectedItems));

  if (isSelected && childStates.every((state) => state === "checked")) {
    return "checked";
  }

  if (!isSelected && childStates.every((state) => state === "unchecked")) {
    return "unchecked";
  }

  return "partially";
}
