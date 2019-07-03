/* eslint-disable */
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
    .force('charge', d3.forceManyBody().strength(d => strengthByType(d)))
    .force('link', d3.forceLink())
    .force('x', d3.forceX(dimensions[0] / 2))
    .force('y', d3.forceY(dimensions[1] / 2));

  const view = HierarchyView.hierarchyView(container)
    .simulation(simulation)
    .onNodeClick(onNodeClick)
    .captionFunction(deriveNodeName)
    .deriveIconClass(myDeriveIconClass);

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
  const data = d.data;

  const detailBox = d3.select('.detailBox');
  detailBox.selectAll('*').remove();
  const contentWrapper = detailBox
    .append('div')
    .classed('content-wrapper', true);
  contentWrapper
    .append('a')
    .attr('href', '#')
    .on('click', () => closeDetailBox())
    .classed('close-popup', true)
    .text('X');

  const table = contentWrapper
    .append('table');

  table
    .append('tr')
    .append('th')
    .attr('colspan', 2)
    .text(data.name);

  if (Utils.isSystemType(d)) {
    const cell = table
    .append('tr')
    .append('td')
    .attr('colspan', 2)
    .append('a')
    .attr('href', '/rhn/systems/details/Overview.do?sid=' + data.rawId)
    .attr('target', '_blank')
    .html('<i class="fa fa-link"></i>System details page');
  }

  const typeRow = table
    .append('tr');
  typeRow
    .append('td')
    .text('Type');
  typeRow
    .append('td')
    .append('strong')
    .text(data.type);

  if (Utils.isCompliantToSSM(d)) {
    const row = table
      .append('tr');
    row
      .append('td')
      .text(t('Add/remove system from SSM'));
    const ssmCell = row
      .append('td');
    ssmCell
      .append('button')
      .classed('detail-box-button addToSSM', true)
      .on('click', () => addSystemToSSM([data.rawId]))
      .html('<i class="fa fa-plus"></i>');
    ssmCell
      .append('button')
      .classed('detail-box-button removeFromSSM', true)
      .on('click', () => removeSystemFromSSM([data.rawId]))
      .html('<i class="fa fa-minus"></i>');
  }

  if (Utils.isSystemType(d)) {
    appendSimpleRow('Base entitlement', cell => cell.text(data.base_entitlement));
    appendSimpleRow('Base channel', cell => cell.text(data.base_channel));
    appendSimpleRow('Checkin time', cell => cell
        .append('time')
        .attr('title', moment(data.checkin).format('LLLL'))
        .text(moment(data.checkin).fromNow()));
    appendSimpleRow('Installed products', cell =>cell.text(data.installedProducts));
    appendSimpleRow('Patch status', cell => appendPatchStatus(cell, data.patch_counts));

    //data.patchCounts
    function appendPatchStatus(cell, patchCountsArray) {
      if (patchCountsArray == undefined) {
        return cell.text('unknown');
      }
      if (patchCountsArray[2] > 0) {
        cell
          .append('div')
          .classed('security-patches', true)
          .html('<i class="fa fa-shield"></i>' + patchCountsArray[2] + t('  security advisories'));
      }
      if (patchCountsArray[0] > 0) {
        cell
          .append('div')
          .classed('bug-patches', true)
          .html('<i class="fa fa-bug"></i>' + patchCountsArray[0] + t('  bug fix advisories'));
      }

      if (patchCountsArray[1] > 0) {
        cell
          .append('div')
          .classed('minor-patches', true)
          .html('<i class="fa spacewalk-icon-enhancement"></i>' + patchCountsArray[1] + t('  product enhancement advisories'));
      }
    }
  }

  if (data.type == 'group' && data.groups != undefined) {
    appendSimpleRow('Groups', cell => cell.text(data.groups
          .map((g, idx) => idx == 0 ? g : ' and ' + g)
          .reduce((a,b) => a + b, '')));
  }

  // valueFn = function invoked on the value cell selection - we use it to
  // fill in various content
  function appendSimpleRow(key, valueFn) {
    const row = table
      .append('tr');
    row
      .append('td')
      .text(key);

    // invoke value function on the new cell
    valueFn(row
        .append('td')
        .append('strong'));
  }

  if (d.children) {
    table
      .append('tr')
      .append('td')
      .attr('colspan', 2)
      .append('button')
      .classed('detail-box-button', true)
      .on('click', () => {
        const idsArray = d.leaves()
          .filter(Utils.isCompliantToSSM)
          .map(system => system.data.rawId);
        const uniqueIds = new Set(idsArray);
        addSystemToSSM(Array.from(uniqueIds));
      })
      .text(t('Add children to SSM'));
  }
}

function closeDetailBox() {
  const detailBox = d3.select('.detailBox');
  detailBox.selectAll('*').remove();
  unselectAllNodes();
}

function addSystemToSSM(ids) {
  return update_server_set('ids', 'system_list', true, ids);
}

function removeSystemFromSSM(ids) {
  return update_server_set('ids', 'system_list', false, ids);
}

function deriveNodeName(d) {
  return (d.data.type && !['system', 'proxy'].includes(d.data.type) ? d.data.name : '') + countChildren(d);
}

function countChildren(node) {
  return node._allChildren ? ' [' + node.children.length + '/' + node._allChildren.length + ']' : '';
}

function myDeriveClass(node) {
  if (node.id == 'root') {
    return 'root';
  }
  if (node.data.type == 'vhm') {
    return 'vhm';
  }
  if (view == 'proxy-hierarchy' && node.data.type == 'proxy' || node.data.type == 'group') {
    return 'inner-node';
  }

  return 'system';
}

function myDeriveIconClass(node) {
  let iconClass = '';
  if (Utils.isSystemType(node)) {
    iconClass = 'spacewalk-icon-desktop-filled';
  }
  else if (node.data.type == 'vhm') {
    iconClass = 'spacewalk-icon-virtual-host-manager';
  }
  else if (node.data.id == 'root') {
    iconClass = 'spacewalk-icon-suse-manager';
  }
  else if (node.data.type == 'group') {
    iconClass = 'spacewalk-icon-desktop-filled-group';
  }
  else {
    iconClass = 'fa-question-circle';
  }
  return iconClass;
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

// Simulation strength based on the node type
function strengthByType(node) {
  let force;
  if (node.data.id == 'root') {
    force = -1800;
  }
  else if (view == 'proxy-hierarchy' && node.data.type == 'proxy' || ['vhm', 'group'].includes(node.data.type)) {
    force = -900;
  }
  else if (Utils.isSystemType(node)) {
    force = -300;
  }
  else {
    force = -500;
  }
  return force;
}

module.exports = {
    dataTree: dataTree
}
