// criterion is a function f(node) => string
// returns a css class
function criteria(initCriteria) {
  const my = {};
  const criteria = initCriteria || {};

  my.get = function(c) {
    return criteria;
  }

  my.deriveClass = function(d) {
    return Object.values(criteria)
      .map(f => f(d) || '')
      .reduce((v1, v2) => v1 + ' ' + v2, '')
      .trim();
  }

  return my;
}

module.exports = {
  criteria: criteria
}

