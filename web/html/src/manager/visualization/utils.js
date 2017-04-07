"use strict";

function computeSvgDimensions() {
  const width = d3.select('#svg-wrapper').node().getBoundingClientRect().width - 2;
  const height = d3.select('.spacewalk-main-column-layout').node().getBoundingClientRect().height - 2 -
    d3.select('#breadcrumb').node().getBoundingClientRect().height -
    d3.select('section .spacewalk-toolbar-h1').node().getBoundingClientRect().height - 200;

  return [width, height];
}

module.exports = {
    computeSvgDimensions: computeSvgDimensions
}

