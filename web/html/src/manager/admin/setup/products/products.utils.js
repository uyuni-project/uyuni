const _isEmpty = require("lodash/isEmpty");

export function searchCriteriaInExtension(baseExtension, criteria) {
  if (criteria) {
    function checkExtension(extension) {
      const isCriteriaInLabel = extension.label && (extension.label).toLowerCase().includes(criteria.toLowerCase());
      const isCriteriaInChannels = !_isEmpty(extension.channels) &&
        extension.channels.some(c => c.summary && (c.summary).toLowerCase().includes(criteria.toLowerCase()));
      return isCriteriaInLabel || isCriteriaInChannels;
    }

    // returns true, if at least one extension matches the criteria
    function extensionRecursiveIterator(extension) {
      return checkExtension(extension) ||
        (!_isEmpty(extension.extensions) && extension.extensions.some(ext => extensionRecursiveIterator(ext)));
    }

    return extensionRecursiveIterator(baseExtension);
  }

  return true;
}
