import _isEmpty from "lodash/isEmpty";

export type ProductFilterOptions = {
  archCriteria?: string[];
  showInstalledOnly?: boolean;
  showSelectedOnly?: boolean;
  selectedItems?: { identifier?: string }[];
};

function checkExtension(extension: any, criteria: string) {
  const isCriteriaInLabel = extension.label && extension.label.toLowerCase().includes(criteria.toLowerCase());
  const isCriteriaInChannels =
    !_isEmpty(extension.channels) &&
    extension.channels.some((c) => c.summary && c.summary.toLowerCase().includes(criteria.toLowerCase()));
  return isCriteriaInLabel || isCriteriaInChannels;
}

// returns true, if at least one extension matches the criteria
function extensionRecursiveIterator(extension: any, criteria: string) {
  return (
    checkExtension(extension, criteria) ||
    (!_isEmpty(extension.extensions) && extension.extensions.some((ext) => extensionRecursiveIterator(ext, criteria)))
  );
}

export function searchCriteriaInExtension(baseExtension: any, criteria?: string): boolean {
  if (criteria) {
    return extensionRecursiveIterator(baseExtension, criteria);
  }
  return true;
}

export function filterProducts(data: any[] = [], filters: ProductFilterOptions = {}) {
  const { archCriteria = [], showInstalledOnly = false, showSelectedOnly = false, selectedItems = [] } = filters;

  let filtered = [...data];

  if (archCriteria.length > 0) {
    filtered = filtered.filter((product) => archCriteria.includes(product.arch));
  }

  if (showInstalledOnly || showSelectedOnly) {
    filtered = filtered.filter((product) => {
      const isInstalled = showInstalledOnly && product.status === "INSTALLED";
      const isSelected = showSelectedOnly && selectedItems.some((item) => item.identifier === product.identifier);

      return isInstalled || isSelected;
    });
  }

  return filtered;
}
