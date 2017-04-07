"use strict";

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
  var root = rootIn || {}; // todo something else
  var deriveClass = (d) => '';
  var simulation = null;

  function my() {
  }

  my.refresh = function() {
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
      .text(d => (d.data.type && d.data.type != 'system' ? d.data.name : '') + countChildren(d));

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

        container.selectAll('text.caption')
          .data(nodes, function(d) { return d.id; })
          .attr('transform', d => 'translate(' + d.x + ',' + d.y + ')');

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

  my.root = function(r) {
    return arguments.length ? (root = r, my) : root;
  }

  my.simulation = function(s) {
    return arguments.length ? (simulation = s, my) : simulation;
  }

  my.deriveClass = function(f) {
    return arguments.length ? (deriveClass = f, my) : deriveClass;
  }

  return my;
}

function unselectAllNodes() {
  d3.selectAll('g.node.selected')
    .each(function(d) {
      var classList = d3.select(this).attr('class');
      d3.select(this).attr('class', classList.replace('selected', ''));
    });
}

function updateSelectedNode(node) {
  unselectAllNodes();
  // select the clicked node
  var classList = d3.select(node).attr('class');
  d3.select(node).attr('class', classList + ' selected');
}

$.closeDetailBox = function() {
  $('.detailBox').hide().html('');
  unselectAllNodes();
}
$.addSystemFromSSM = function(ids) {
  return update_server_set('ids', 'system_list', true, ids);
}

function isSystemType(d) {
  return d.data.type && d.data.type == 'system' && d.data.rawId != '' && d.data.id != 'root';
}

function updateDetailBox(d) {
  function patchStatus(patchCountsArray) {
    if (patchCountsArray == undefined) {
      return 'unknown';
    }
    const unknownCountMsg = 'unknown count of';
    return (patchCountsArray[0] || unknownCountMsg) + ' bug fix advisories, ' +
      (patchCountsArray[1] || unknownCountMsg) + ' product enhancement advisories, ' +
      (patchCountsArray[2] || unknownCountMsg) + ' security advisories.';
  }

  var data = d.data;
  var systemDetailLink = '';
  var systemSpecificInfo = '';
  var systemToSSM = '';
  if (isSystemType(d)) {
    systemDetailLink = '<div><a href="/rhn/systems/details/Overview.do?sid=' +
      data.rawId + '" target="_blank">System details page</a></div>';

    systemToSSM = '<button class="btn btn-default" onClick="$.addSystemFromSSM([' + data.rawId + '])">Add system to SSM</button>';
    systemSpecificInfo =
      '<div>Base entitlement : <strong>' + data.base_entitlement + '</strong></div>' +
      '<div>Base channel: <strong>' + data.base_channel + '</strong></div>' +
      '<div>Checkin time : <strong><time title="' + moment(data.checkin).format('LLLL') + '">' + moment(data.checkin).fromNow() + '</time></strong></div>' +
      '<div>Installed products : <strong>' + data.installedProducts + '</strong></div>' +
      '<div>Patch status : <strong>' + patchStatus(data.patch_counts) + '</strong></div>';
  }
  let groupSpecificInfo = '';
  if (data.type == 'group' && data.groups != undefined) {
    groupSpecificInfo = '<div>Groups: <b>' + data.groups
      .map((g, idx) => idx == 0 ? g : ' and ' + g)
      .reduce((a,b) => a + b, '') + '</b></div>';
  }
  $('.detailBox').html(
    '<div class="content-wrapper">' +
    '<a href="#" class="close-popup" onClick="$.closeDetailBox()">X</a>' +
    '<div>System name : <strong>' + data.name + '</strong></div>' +
    systemDetailLink +
    systemToSSM +
    '<div>Type : <strong>' + data.type + '</strong></div>' +
    systemSpecificInfo +
    groupSpecificInfo +
    '</div>'
  ).show();

}

function countChildren(node) {
  return node._allChildren ? ' [' + node.children.length + '/' + node._allChildren.length + ']' : '';
}

module.exports = {
    hierarchyView: hierarchyView,
    isSystemType: isSystemType
}
