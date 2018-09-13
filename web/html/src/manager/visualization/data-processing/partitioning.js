/* eslint-disable */
'use strict';

// Holds functions needed for computing node partitions (user e.g. for deriving
// CSS class)
function partitioning(initPartitioning) {
  const my = {};
  const partitioning = initPartitioning || {};

  // get the partitioning functions
  my.get = function(c) {
    return partitioning;
  }

  // compute the partition name
  my.computePartitionName = function(d) {
    return Object.values(partitioning)
      .map(f => f(d) || '')
      .reduce((v1, v2) => v1 + ' ' + v2, '')
      .trim();
  }

  return my;
}

module.exports = {
  partitioning: partitioning
}
