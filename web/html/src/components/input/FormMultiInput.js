// @flow
import * as React from 'react';

/**
 * Compute the list of items in the model based of fields named like `${prefix}${idx}_${name}`
 */
export function getOrderedItemsFromModel(model: Object, prefix: string): Array<number> {
  return Object.keys(model)
    .map(property => {
      const result = property.match(new RegExp(`^${prefix}([0-9]+)`));
      if (result != null) {
        return Number.parseInt(result[1]);
      }
      return -1;
    })
    // only return one of each matching properties
    .filter((property, index, array) => property >= 0 && index === array.indexOf(property))
    .sort();
}
