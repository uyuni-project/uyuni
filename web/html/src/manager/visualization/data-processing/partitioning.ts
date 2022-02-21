// Holds functions needed for computing node partitions (user e.g. for deriving
// CSS class)
function partitioning<T>(initPartitioning?: T) {
  const my: any = {};
  const partitioning = initPartitioning || {};

  // get the partitioning functions
  my.get = function (c) {
    return partitioning;
  };

  // compute the partition name
  my.computePartitionName = function (d) {
    return Object.values(partitioning)
      .map((f: any) => f(d) || "")
      .reduce((v1, v2) => v1 + " " + v2, "")
      .trim();
  };

  return my;
}

export { partitioning };
