/* eslint-disable */
'use strict';

// Stores filters
function filters(initFilters) {
  const filters = initFilters || {};
  const my = {};

  my.put = function(filterName, filter) {
    filters[filterName] = filter;
  }

  my.remove = function(filterName) {
    delete filters[filterName];
  }

  // construct a predicate function from filters
  // (i.e. return a  function that connects all predicates with logical AND)
  my.predicate = function() {
    const fs = Object.values(filters);
    return function(node) {
      return fs.map(f => f(node)).reduce((v1, v2) => v1 && v2, true)
    }
  }

  return my;
}

module.exports = {
    filters: filters
}
