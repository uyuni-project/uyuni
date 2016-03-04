"use strict";

const React = require("react")

const mappingComparator = (comparator, map) => (a, b) => comparator(map(a), map(b));
const mappingFilter = (filter, map) => (cell, filterValue) => filter(map(cell), filterValue);

const stringLocaleComparator = (a, b) => a.localeCompare(b);
const numberComparator = (a, b) => a > b ? 1 : a === b ? 0 : -1;

const lengthComparator = mappingComparator(numberComparator, (x) => x.length);

const stringSubstringFilter = (cell, filterValue) => cell.indexOf(filterValue) > -1;

const generateSubstringHighlightRenderer = (match, nomatch, container) => (cell, filter) => {
  const elements = cell
      .split(filter)
      .map((x, i) => [nomatch(x, i)])
      .reduce((p, c, i) => p.concat([match(filter, i)].concat(c)))
  return container(elements)
}

const stringSubstringHighlight = generateSubstringHighlightRenderer(
    (match, index) => <span key={"m"+index} style={{backgroundColor: "#f0ad4e", borderRadius: "2px"}}>{ match }</span>,
    (nomatch, index) => <span key={"n"+index}>{ nomatch }</span>,
    (elements) => <b>{ elements }</b>
);

module.exports = {
    Comparators : {
        mapping: mappingComparator,
        locale: stringLocaleComparator,
        number: numberComparator,
        length: lengthComparator
    },
    Filters: {
        mapping: mappingFilter,
        substring: stringSubstringFilter
    },
    Renderer: {
        generate: generateSubstringHighlightRenderer,
        highlightSubstring: stringSubstringHighlight
    }
}
