'use strict';

const HierarchyView = require('./ui/hierarchy-view.js');
const Filters = require('./data-processing/filters.js');
const Partitioning = require('./data-processing/partitioning.js');
const Preprocessing = require('./data-processing/preprocessing.js');
const Utils = require('./utils.js');

// Render hierarchy view - take data, transform with preprocessor, filters and
// partitioning, render it in the container.
//
// Compulsory parameters:
//  - data - data from the server to be rendered
//  - container - where the tree will be rendered in the DOM
//
// Other parameters (settable via methods):
//  - preprocessors - preparing the data so that it can be rendered by d3
//  - filters - showing/hiding of nodes based on customized conditions
//  - partitioning - displaying nodes in partitions with different CSS classes
//  based on custom conditions
//  - simulation - d3 force simulation
//
// Methods
//  - refresh - transforms the data according to current preprocessor, filters
//  and partitioning settings and refresh the DOM
//
function dataTree(data, container) {

  let preprocessor = Preprocessing.stratify();
  const dimensions = Utils.computeSvgDimensions();

  let filters = Filters.filters();
  let partitioning = Partitioning.partitioning();

  partitioning.get()['default'] = myDeriveClass;

  let simulation = d3.forceSimulation()
    .force('charge', d3.forceManyBody().strength(d => strengthFromDepth(d.depth)))
    .force('link', d3.forceLink())
    .force('x', d3.forceX(dimensions[0] / 2))
    .force('y', d3.forceY(dimensions[1] / 2));

  const view = HierarchyView.hierarchyView(container)
    .simulation(simulation)
    .onNodeClick(onNodeClick)
    .captionFunction(deriveNodeName);

  function instance() {
  }

  instance.data = function(d) {
    return arguments.length ? (data = d, instance) : data;
  }

  instance.preprocessor = function(p) {
    return arguments.length ? (preprocessor = p, instance) : preprocessor;
  }

  instance.filters = function(f) {
    return arguments.length ? (filters = f, instance) : filters;
  }

  instance.partitioning = function(p) {
    return arguments.length ? (partitioning = p, instance) : partitioning;
  }

  instance.view = function() {
    return view;
  }

  // Recomputes the data transformation (preprocessor, filters, partitioning),
  // turns the data to tree and refreshes the hierarchy view.
  instance.refresh = function() {
    preprocessor.data(data);
    const newRoot = preprocessor();
    treeify(newRoot, dimensions);
    view.root(newRoot);
    nodeVisible(newRoot, filters.predicate());
    view.deriveClass(partitioning.computePartitionName)
    view.refresh();
  }

  instance.simulation = function(sim) {
    return arguments.length ? (simulation = sim, instance) : simulation;
  }

  return instance;
}

//
// UTILS FUNCTIONS
//

function onNodeClick(d) {
  updateSelectedNode(this);
  updateDetailBox(d);
}

function updateSelectedNode(node) {
  unselectAllNodes();
  // select the clicked node
  const select = d3.select(node);
  const classList = select.attr('class');
  select.attr('class', classList + ' selected');
}

function unselectAllNodes() {
  d3.selectAll('g.node.selected')
    .each(function(d) {
      const classList = d3.select(this).attr('class');
      d3.select(this).attr('class', classList.replace('selected', ''));
    });
}

function updateDetailBox(d) {
  function patchStatus(patchCountsArray) {
    if (patchCountsArray == undefined) {
      return 'unknown';
    }
    const unknownCountMsg = '<span title="unknown count of">?</span>';
    return (patchCountsArray[0] > 0 ? '<div>○ ' + patchCountsArray[0] + ' bug fix advisories</div>' : '') +
      (patchCountsArray[1] > 0 ? '<div>○ ' + patchCountsArray[1] + ' product enhancement advisories</div>' : '') +
      (patchCountsArray[2] > 0 ? '<div>○ ' + patchCountsArray[2] + ' security advisories</div>' : '');
  }

  const data = d.data;
  let systemDetailLink = '';
  let systemSpecificInfo = '';
  let systemToSSM = '';
  if (Utils.isSystemType(d)) {
    systemDetailLink = '<tr><td colspan="2"><a href="/rhn/systems/details/Overview.do?sid=' +
      data.rawId + '" target="_blank"><i class="fa fa-link"></i>System details page</a></td></tr>';

    systemToSSM = '<tr><td>Add/remove system from SSM</td>' +
      '<td><div class="input-group"><button class="input-group-addon addToSSM" onClick="$.addSystemFromSSM([' + data.rawId + '])"><i class="fa fa-plus"></i></button>' +
      '<button" class="input-group-addon removeFromSSM" onClick="$.removeSystemFromSSM([' + data.rawId + '])"><i class="fa fa-minus"></i></button></div></td></tr>';

    systemSpecificInfo =
      '<tr><td>Base entitlement</td><td><strong>' + data.base_entitlement + '</strong></td></tr>' +
      '<tr><td>Base channel</td><td><strong>' + data.base_channel + '</strong></td></tr>' +
      '<tr><td>Checkin time</td><td><strong><time title="' + moment(data.checkin).format('LLLL') + '">' + moment(data.checkin).fromNow() + '</time></strong></td></tr>' +
      '<tr><td>Installed products</td><td><strong>' + data.installedProducts + '</strong></td></tr>' +
      '<tr><td>Patch status</td><td><strong>' + patchStatus(data.patch_counts) + '</strong></td></tr>';
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
      '<table><tr><th colspan="2">' + data.name + '</th></tr>' +
      systemDetailLink +
      systemToSSM +
      '<tr><td>Type</td><td><strong>' + data.type + '</strong></td></tr>' +
      systemSpecificInfo +
      groupSpecificInfo +
      '</table></div>'
      ).show();

}

$.closeDetailBox = function() {
  $('.detailBox').hide().html('');
  unselectAllNodes();
}

$.addSystemFromSSM = function(ids) {
  return update_server_set('ids', 'system_list', true, ids);
}

$.removeSystemFromSSM = function(ids) {
  return update_server_set('ids', 'system_list', false, ids);
}

function deriveNodeName(d) {
  return (d.data.type && d.data.type != 'system' ? d.data.name : '') + countChildren(d);
}

function countChildren(node) {
  return node._allChildren ? ' [' + node.children.length + '/' + node._allChildren.length + ']' : '';
}

function myDeriveClass(node) {
  if (node.id == 'root') {
    return 'root';
  }

  if (view == 'proxy-hierarchy' && node.depth == 1 || ['group', 'vhm'].includes(node.data.type)) {
    return 'inner-node';
  }

  return 'system';
}

// turns the input data into tree, backups children
function treeify(root, dimensions) {
  const tree = d3.tree().size(dimensions);
  tree(root);
  root.each(d => {
      d._allChildren = d.children;  // backup children
  })
}

function nodeVisible(node, pred) {
  // dfs
  if (!node.children) {
    return pred(node);
  }

  const visibleChildren = node._allChildren.filter(c => nodeVisible(c, pred));
  node.children = visibleChildren;
  return visibleChildren.length > 0 || pred(node);
}

// Simulation strength based on the node depth
function strengthFromDepth(depth) {
  switch (depth) {
    case 0: return -450;
    case 1: return -270;
    default: return -135;
  }
}

module.exports = {
    dataTree: dataTree
}

