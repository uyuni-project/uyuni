'use strict';

const Utils = require('./utils.js');

function computeSvgDimensions() {
  const width = d3.select('#svg-wrapper').node().getBoundingClientRect().width - 2;
  const height = d3.select('.spacewalk-main-column-layout').node().getBoundingClientRect().height - 2 -
    d3.select('#breadcrumb').node().getBoundingClientRect().height -
    d3.select('section .spacewalk-toolbar-h1').node().getBoundingClientRect().height - 200;

  return [width, height];
}

function prepareDom() {
  const dimensions = computeSvgDimensions();
  const svg = d3.select('#svg-wrapper')
    .append('svg')
    .attr('width', dimensions[0])
    .attr('height', dimensions[1]);
  const container = svg.append('g');

  // Zoom handling
  svg.call(d3.zoom()
      .scaleExtent([1 / 8, 16])
      .on('zoom', zoomed))
    .on('dblclick.zoom', null);
  function zoomed(d) {
    container.attr('transform', d3.event.transform);
  }

  return container;
}

function isSystemType(d) {
  return d.data.type && d.data.type == 'system' && d.data.rawId != '' && d.data.id != 'root';
}

function isCompliantToSSM(d) {
  return isSystemType(d) && ['enterprise_entitled', 'salt_entitled', 'bootstrap_entitled'].includes(d.data.base_entitlement);
}

module.exports = {
    computeSvgDimensions: computeSvgDimensions,
    prepareDom: prepareDom,
    isSystemType: isSystemType,
    isCompliantToSSM: isCompliantToSSM
}
