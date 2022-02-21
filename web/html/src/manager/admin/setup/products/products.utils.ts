import _isEmpty from "lodash/isEmpty";

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
