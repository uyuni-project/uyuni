"use strict";

// Display given hierarchy (given by it's root node) in given container using
// force based layout.
//
// - compulsory parameters:
//  - root - root of the hierarchy to display
//  - container - DOM node where the animation will be placed
//
// Functions for deriving node classes (e.g. for dynamic css styles based on
// node parameters) and for customizing force simulation are settable using
// deriveClass and simulation functions.
//
function hierarchyView(root, container) {
  // default params
  var deriveClass = (d) => '';
  var simulation = null;

  function my() {
    var nodes = root.descendants();
    var links = root.links();

    var node = container.selectAll('g.node')
      .data(nodes, function(d) { return d.id; });

    node
      .exit()
      .remove();

    var gEnter = node
      .enter()
      .append('g')
      .on('click', function(d) {
        updateSelectedNode(this);
        // update the detail box with the clicked node data
        updateDetailBox(d);
      })

    gEnter
      .append('circle')
      .attr('r', 5); // default

    gEnter
      .append("text")
      .attr("dx", "1em")
      .attr("dy", ".15em")
      .text(d => d.data.type && d.data.type != 'system' ? d.data.name : '');

    // common for enter + update sections
    node
      .merge(gEnter)
      .attr('class', d => 'node ' + deriveClass(d));

    var link = container.selectAll('line.link').data(links, d => d.target.id);

    link
      .exit()
      .remove();

    link = link
      .enter()
      .insert("line", "g")
      .attr("class", "link");

    // feed simulation with data
    if (simulation != null) {
      // refresh the update section
      node = container.selectAll('g')
        .data(nodes, function(d) { return d.id; })
      link = container.selectAll('line.link')
        .data(links, d => d.target.id);

      simulation.nodes(nodes);
      simulation.force('link').links(links);
      simulation.on('tick', () => {
        node
        .each(d => {
          if (d.data.partition) {
            d.x += simulation.alpha() * 6;
          } else {
            d.x -= simulation.alpha() * 6;
          }
        });
        node
          .attr('transform', d => 'translate(' + d.x + ',' + d.y + ')')
        link
          .attr("class", "link")
          .attr("x1", s => s.source.x)
          .attr("y1", s => s.source.y)
          .attr("x2", s => s.target.x)
          .attr("y2", s => s.target.y);
      }
      );
      simulation.alpha(1).restart();
    }
  }

  my.simulation = function(s) {
    return arguments.length ? (simulation = s, my) : simulation;
  }

  my.deriveClass = function(f) {
    return arguments.length ? (deriveClass = f, my) : deriveClass;
  }

  return my;
}

function updateSelectedNode(node) {
  // unselect all
  d3.selectAll('g.node.selected')
    .each(function(d) {
      var classList = d3.select(this).attr('class');
      d3.select(this).attr('class', classList.replace('selected', ''));
    });
  // select the clicked node
  var classList = d3.select(node).attr('class');
  d3.select(node).attr('class', classList + ' selected');
}

function updateDetailBox(d) {
  var data = d.data;
  var systemDetailLink = '';
  if (data.type && data.type == 'system' && data.id != 'root') {
    var idSlices = (data.id).split('-');
    var systemId = idSlices[idSlices.length - 1];
    systemDetailLink = '<div>System details page: <a href="/rhn/systems/details/Overview.do?sid=' +
      systemId + '" target="_blank">' + data.name + '</a></div>';
  }
  $('.detailBox').html(
    systemDetailLink +
    '<div>System name : <strong>' + data.name + '</strong></div>' +
    '<div>Type : <strong>' + data.type + '</strong></div>' +
    '<div>Base entitlement : <strong>' + data.base_entitlement + '</strong></div>' +
    '<div>Base channel: <strong>' + data.base_channel + '</strong></div>' +
    '<div>Checkin time : <strong>' + new Date(data.checkin) + '</strong></div>').show()
    .css('top', (window.mouseY || 0) - $('.detailBox').height()).css('left', (window.mouseX || 0) + 20);
}

module.exports = {
    hierarchyView: hierarchyView
}
