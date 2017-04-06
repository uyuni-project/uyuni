'use strict';

const Utils = require('../utils.js');

// Display given hierarchy (given by it's root node) in given container using
// force based layout.
//
// - compulsory parameters:
//  - container - DOM node where the animation will be placed
// - optional parameters
//  - rootIn - root of the hierarchy to display (settable via root method)
//
// Functions for deriving node classes (e.g. for dynamic css styles based on
// node parameters) and for customizing force simulation are settable using
// deriveClass and simulation functions.
//
function hierarchyView(container, rootIn) {
  // default params
  let root = rootIn;
  let deriveClass = (d) => '';
  let simulation = null;
  let onNodeClick = () => {};
  let captionFunction = d => d.data.name;

  function my() {
  }

  my.refresh = function() {
    const nodes = root.descendants();
    const links = root.links();

    const node = container.selectAll('g.node')
      .data(nodes, function(d) { return d.id; });

    node
      .exit()
      .remove();

    const gEnter = node
      .enter()
      .append('g')
      .on('click', onNodeClick);

    gEnter
      .append('svg:foreignObject')
      .html(d => appendIconType(d));

    // common for enter + update sections
    node.merge(gEnter)
      .attr('class', d => 'node ' + deriveClass(d));

    // text captions are handled separately so that they'll render on the top of other elements in SVG
    // remove all text captions to re-render them on the top
    container.selectAll('text.caption')
      .remove();
    // we only care about the 'enter' section as we have removed all captions above
    container.selectAll('text.caption')
      .data(nodes.filter(d => d.children != null), function(d) { return d.id; })
      .enter()
      .append('text')
      .attr('class', 'caption')
      .attr('dx', '1em')
      .attr('dy', '.15em')
      .text(captionFunction);

    const link = container.selectAll('line.link').data(links, d => d.target.id);

    link
      .exit()
      .remove();

    link
      .enter()
      .insert('line', 'g')
      .attr('class', 'link');

    // feed simulation with data
    if (simulation != null) {
      // refresh the update section
      const node = container.selectAll('g')
        .data(nodes, function(d) { return d.id; })
      const link = container.selectAll('line.link')
        .data(links, d => d.target.id);

      simulation.nodes(nodes);
      simulation.force('link').links(links);
      simulation.on('tick', () => {
        node.each(d => {
          if (d.data.partition) {
            d.x += simulation.alpha() * 6;
          } else {
            d.x -= simulation.alpha() * 6;
          }
        });
        node
          .attr('transform', d => 'translate(' + d.x + ',' + d.y + ')')

        container.selectAll('text.caption')
          .data(nodes, function(d) { return d.id; })
          .attr('transform', d => 'translate(' + d.x + ',' + d.y + ')');

        link
          .attr('class', 'link')
          .attr('x1', s => s.source.x)
          .attr('y1', s => s.source.y)
          .attr('x2', s => s.target.x)
          .attr('y2', s => s.target.y);
      }
      );
      simulation.alpha(1).restart();
    }
  }

  my.root = function(r) {
    return arguments.length ? (root = r, my) : root;
  }

  my.simulation = function(s) {
    return arguments.length ? (simulation = s, my) : simulation;
  }

  my.deriveClass = function(f) {
    return arguments.length ? (deriveClass = f, my) : deriveClass;
  }

  my.onNodeClick = function(c) {
    return arguments.length ? (onNodeClick = c, my) : onNodeClick;
  }

  my.captionFunction = function(f) {
    return arguments.length ? (captionFunction = f, my) : captionFunction;
  }

  return my;
}

// todo move
const knownTypes = ['root', 'vhm', 'system'];
function isUnknownType(d) {
  return !d.data.type || knownTypes.indexOf(d.data.type) == -1;
}

function appendIconType(node) {
  var iconClass = '';
  var iconContent = '';
  if (Utils.isSystemType(node)) {
    iconClass = 'fa-desktop';
  }
  else if (node.data.type == 'vhm') {
    iconClass = 'spacewalk-icon-virtual-host-manager';
  }
  else if (node.data.id == 'root') {
    iconClass = 'spacewalk-icon-suma';
  }
  else {
    iconClass = 'fa-question-circle';
  }
  return '<i class="fa ' + iconClass + '">' + iconContent + '</i>'
}

module.exports = {
    hierarchyView: hierarchyView
}

