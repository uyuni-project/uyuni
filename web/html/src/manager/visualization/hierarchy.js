"use strict";

const Network = require("../../utils/network");
const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../../components/panel").Panel;
const HierarchyView = require("./hierarchy-view.js");
const Filters = require("./filters.js");
const Criteria = require("./criteria.js");
const Preprocessing = require("./preprocessing.js");

// turns the input data into tree, backups children
function treeify(root, dimensions) {
  const tree = d3.tree().size(dimensions);
  tree(root);
  root.each(d => {
      d._allChildren = d.children;  // backup children
  })
}


// Returns a value bound to the depth level of the node
// todo move?
function distanceFromDepth(depth) {
  switch (depth) {
    case 0: return 300;
    case 1: return 180;
    default: return 90;
  }
}

function computeSvgDimensions() {
  const width = d3.select('#svg-wrapper').node().getBoundingClientRect().width - 2;
  const height = d3.select('.spacewalk-main-column-layout').node().getBoundingClientRect().height - 2 -
    d3.select('#breadcrumb').node().getBoundingClientRect().height -
    d3.select('section .spacewalk-toolbar-h1').node().getBoundingClientRect().height - 200;

  return [width, height];
}

// Render hierarchy view
// - root
// - container
// - deriveClass
// - custom simulation
// purpose: give data, filters, everything, re-render the tree
function customTree(root, container, deriveClass) {

  let filters = Filters.filters();
  let criteria = Criteria.criteria();

  const dimensions = computeSvgDimensions();
  let simulation = d3.forceSimulation()
    .force("charge", d3.forceManyBody().strength(d => -distanceFromDepth(d.depth) * 1.5))
    .force("link", d3.forceLink())
    .force("x", d3.forceX(dimensions[0] / 2))
    .force("y", d3.forceY(dimensions[1] / 2));

  const tree = HierarchyView.hierarchyView(root, container)
    .simulation(simulation)
    .deriveClass(deriveClass);

  function instance() {
  }

  instance.filters = function(f) {
    return arguments.length ? (filters = f, instance) : filters;
  }

  instance.criteria = function(c) {
    return arguments.length ? (criteria = c, instance) : criteria;
  }

  instance.root = tree.root;

  instance.deriveClass = tree.deriveClass;

  instance.refreshTree = function() {
    tree();
  }

  instance.simulation = function(sim) {
    return arguments.length ? (simulation = sim, instance) : simulation;
  }

  return instance;
}

function initHierarchy() {
  $(document).ready(function() {
    // disable the #spacewalk-content observer:
    // drawing svg triggers it for every small changes,
    // and it is not the desired behaviour/what the observer stands for
    // note: leaving it connected slow down the svg usability
    spacewalkContentObserver.disconnect();

    // Get data & put everything together in the graph!
    Network
      .get(endpoint, "application/json")
      .promise
      .then(d => {
        const dimensions = computeSvgDimensions();

        // Prepare DOM
        var svg = d3.select('#svg-wrapper')
          .append('svg')
          .attr('width', dimensions[0])
          .attr('height', dimensions[1]);
        var container = svg.append("g");

        // Zoom handling
        svg.call(d3.zoom()
          .scaleExtent([1 / 8, 16])
          .on("zoom", zoomed))
          .on("dblclick.zoom", null);
        function zoomed(d) {
          var event = d3.event;
          container.attr("transform", event.transform);
        }

        // Returns the CSS class for the given node
        // simple algorithm based on depth
        var myDeriveClass = function(node) {
          if (node.id == 'root') {
            return 'root';
          }

          if (view == 'proxy-hierarchy' && node.depth == 1 || ['group', 'vhm'].includes(node.data.type)) {
              return 'inner-node';
          }

          return 'system';
        }

        let dataProcessor = Preprocessing.stratify(d);
        if (view == 'grouping') {
          dataProcessor = Preprocessing.grouping(d);
        }
        const root = dataProcessor();
        treeify(root, dimensions);
        const t = customTree(root, container, myDeriveClass);
        t.refreshTree();

        function nodeVisible(node, pred) {
          // dfs
          if (!node.children) {
            return pred(node);
          }

          const visibleChildren = node._allChildren.filter(c => nodeVisible(c, pred));
          node.children = visibleChildren;
          return visibleChildren.length > 0 || pred(node);
        }

        t.criteria().get()['default'] = myDeriveClass;

        const nameFilterDiv = d3.select('#filter-wrapper')
          .append('div').attr('class', 'filter');
        nameFilterDiv
          .append('label')
          .text('Filter by system name');
        nameFilterDiv
          .append('input')
          .attr('type', 'text')
          .attr('placeholder', 'e.g., client.nue.sles')
          .on('input', function() {
            t.filters().put('name', d => d.data.name.toLowerCase().includes(this.value.toLowerCase()));
            refreshTree(dataProcessor, t.criteria(), t);
          });

        const patchCountsFilter = d3.select('#filter-wrapper')
          .append('div').attr('class', 'filter');

        patchCountsFilter
          .append('label')
          .text('Filter by patches:');

        function appendCheckbox(placeholder, caption, callback) {
          const securityAdvisoriesDiv = placeholder
            .append('div');

          securityAdvisoriesDiv
            .append('input')
            .attr('type', 'checkbox')
            .on('change', function() { callback(this.checked); });

          securityAdvisoriesDiv
          .append('label')
          .text(caption);
        }

        // state of the patch status checkboxes:
        // [bug fix adv. checked, prod. enhancements checked, security adv. checked]
        const patchCountFilterConfig = [false, false, false];
        // create a callback function that
        //  - updates patchCountFilterConfig at given index,
        //  - updates the filters based on patchCountFilterConfig
        //  - refreshes the tree
        function patchCountFilterCallback(idx) {
          return function(checked) {
            patchCountFilterConfig[idx] = checked;
            if (!patchCountFilterConfig.includes(true)) {
              t.filters().remove('patch_count_filter');
            } else {
              t.filters().put('patch_count_filter', d => {
                return HierarchyView.isSystemType(d) &&
                  patchCountFilterConfig // based on the checkboxes state, take into account the patch count
                    .map((value, index) => value && (d.data.patch_counts || [])[index] > 0)
                    .reduce((a, b) => a || b, false);
              });
            }
            refreshTree(dataProcessor, t.criteria(), t);
          }
        }
        appendCheckbox(patchCountsFilter, 'has bug fix advisories', patchCountFilterCallback(0));
        appendCheckbox(patchCountsFilter, 'OR has product enhancement advisories', patchCountFilterCallback(1));
        appendCheckbox(patchCountsFilter, 'OR has security advisories', patchCountFilterCallback(2));

        const baseProdFilterDiv = d3.select('#filter-wrapper')
          .append('div').attr('class', 'filter');
        baseProdFilterDiv
           .append('label')
           .text('Filter by system base channel');
        baseProdFilterDiv
          .append('input')
          .attr('type', 'text')
          .attr('placeholder', 'e.g., SLE12')
          .on('input', function() {
            t.filters().put('base_channel', d => (d.data.base_channel || '').toLowerCase().includes(this.value.toLowerCase()));
            refreshTree(dataProcessor, t.criteria(), t);
          });

        const installedProductsFilterDiv = d3.select('#filter-wrapper')
          .append('div').attr('class', 'filter');
        installedProductsFilterDiv
          .append('label')
          .text('Filter by system installed products');
        installedProductsFilterDiv
          .append('input')
          .attr('type', 'text')
          .attr('placeholder', 'e.g., SLES')
          .on('input', function() {
            t.filters().put('installedProducts', d =>  (d.data.installedProducts || []).map(ip => ip.toLowerCase().includes(this.value.toLowerCase())).reduce((v1,v2) => v1 || v2, false));
            refreshTree(dataProcessor, t.criteria(), t);
          });

        function refreshTree(processor, criteria, tree) {
          const newRoot = processor();
          treeify(newRoot, dimensions);
          tree.root(newRoot);
          nodeVisible(newRoot, tree.filters().predicate());
          tree.deriveClass(criteria.deriveClass)
          tree.refreshTree();
        }

        if (dataProcessor.groupingConfiguration) { // we have a processor responding to groupingConfiguration
          const groupingDiv = d3.select('#filter-wrapper')
            .append('div').attr('class', 'filter');
          groupingDiv
            .append('label')
            .text('Split into groups');

          let grps = d
            .map(e => e.managed_groups || [])
            .reduce((a,b) => a.concat(b));
          let mySel = groupSelector(grps, groupingDiv);
          mySel.onChange(function(data) {
            dataProcessor.groupingConfiguration(data);
            refreshTree(dataProcessor, t.criteria(), t);
          });
          mySel();

        }

        function updateTree() {
          const date = $( '#criteria-datepicker' ).datepicker( "getDate" );
          const time = $( '#criteria-timepicker' ).timepicker( "getTime" );

          const dateTime = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
            time.getHours(), time.getMinutes(), time.getSeconds());
          t.criteria().get()['user-criteria'] = d => {
            if (d.data.checkin == undefined) {
              return '';
            }
            var firstPartition = d.data.checkin < dateTime.getTime();
            d.data.partition = firstPartition;
            return firstPartition  ? 'stroke-red' : 'stroke-green';
          };
          refreshTree(dataProcessor, t.criteria(), t);
        }

        function resetTree() {
          t.criteria().get()['user-criteria'] = d => { return ''};
          t.refreshTree();
        }

        const checkinTimeCriteria = d3.select('#filter-wrapper')
          .append('div').attr('class', 'filter');

        checkinTimeCriteria
           .append('label')
           .text('Partition systems by given check-in time:');

        checkinTimeCriteria
          .append('input')
          .attr('id', 'criteria-datepicker')
          .attr('type', 'text');

        checkinTimeCriteria
          .append('input')
          .attr('id', 'criteria-timepicker')
          .attr('type', 'text');

        $('#criteria-datepicker').datepicker({
          autoclose: true,
          format: 'yyyy-mm-dd'
        });
        $('#criteria-datepicker').datepicker('setDate', new Date());
        $('#criteria-timepicker').timepicker({timeFormat: 'H:i:s'});
        $('#criteria-timepicker').timepicker('setTime', new Date());

        checkinTimeCriteria
          .append('button')
          .attr('type', 'button')
          .attr('class', 'btn btn-default')
          .on('click', updateTree)
          .text('Apply');

        const hasPatchesCriteria = d3.select('#filter-wrapper')
          .append('div').attr('class', 'filter');

        hasPatchesCriteria
           .append('label')
           .text('Partition systems based on whether there are patches for them:');

        function applyPatchesCriteria() {
          t.criteria().get()['user-criteria'] = d => {
            if (d.data.patch_counts == undefined) {
              return '';
            }
            var firstPartition = d.data.patch_counts.filter(pc => pc > 0).length > 0;
            d.data.partition = firstPartition;
            return firstPartition  ? 'stroke-red' : 'stroke-green';
          };
          refreshTree(dataProcessor, t.criteria(), t);
        }

        hasPatchesCriteria
          .append('button')
          .attr('type', 'button')
          .attr('class', 'btn btn-default')
          .on('click', applyPatchesCriteria)
          .text('Apply');

        d3.select('#filter-wrapper')
          .append('div').attr('class', 'filter')
          .append('button')
          .attr('type', 'button')
          .attr('class', 'btn btn-default')
          .on('click', resetTree)
          .text('Reset partitioning');

        $(window).resize(function () {
          const dimensions = computeSvgDimensions();
          // try to find the object via d3
          d3.select('#svg-wrapper svg')
            .attr('width', dimensions[0])
            .attr('height', dimensions[1]);
        });

        function addVisibleTreeToSSM() {
          const ids = new Set();
          t.root().each(e => {
              if (HierarchyView.isSystemType(e)) {
                ids.add(e.data.rawId);
              }
            });
          $.addSystemFromSSM(Array.from(ids));
        }

        const addAllToSSMButton = d3.select('#filter-wrapper')
          .append('div')
          .append('button')
          .attr('class', 'btn btn-default')
          .on('click', addVisibleTreeToSSM)
          .text('Add tree to SSM');

      }, (xhr) => {
          d3.select('#svg-wrapper')
            .text(t('There was an error fetching data from the server.'));
      });
  });
}

// Simple JS component for selecting groups
//
// Adds an 'Add a grouping level' button, clicking on it appends a new
// multiselect box in the UI. In each of these select boxes, the user can
// specify multiple groups. Selected options are internally stored in the
// 'data' array in the format:
//  [['grp1', 'grp2'], // selected groups on the 1st level
//   ['grp1', 'grp2']  // selected groups on the 2nd level
//  ]
//
// After a selection is changed, 'onChange' will be called with the new data as
// a parameter.
//
// In addition, each selectbox is accompanied by a 'Remove this level' button
// that deletes it from the UI, adjusts the data and fires the onChange
// callback.
//
// input
//  - groups: array of possible group names (['grp1', 'grp2'])
//  - element: where to hook the UI
// methods
//  - onChange: function that is called after a selection is changed or a
//  select box on one level is collapsed
function groupSelector(groups, element) {
  const data = [];
  let onChange = function(data) { console.log('data changed: ' + data); };
  groups = Array.from(new Set(groups));

  function appendAdder() {
    element
      .append('a')
      .attr('href', '#')
      .text('Add a grouping level')
      .on('click', d => {
        data.push([]);
        update();
      });
  }

  function update() {
    const updateSection = element
      .selectAll('.grpCriterion')
      .data(data, (d, i) => i);

    updateSection.exit().remove();

    const divEnter = updateSection.enter()
      .append('div')
      .attr('class', 'grpCriterion');

    const selectEnter = divEnter
      .append('select')
      .attr('multiple', 'multiple')
      .on('change', function(d, i) {
        const selectedOpts = Array.apply(null, this.options)
          .filter(o => o.selected == true)
          .map(o => [o.value]);
        data[i] = selectedOpts;
        onChange(data);
      });

    selectEnter
      .selectAll('option')
      .data(groups)
      .enter()
      .append('option')
      .attr('value', d => d)
      .text(d => d);

    divEnter
      .append('a')
      .attr('href', '#')
      .text('Remove this level')
      .on('click', function(d, i) {
        data.splice(i, 1);
        onChange(data);
        update();
      });
  }

  function my() {
    appendAdder();
  }

  my.onChange = function(callback) {
    return arguments.length ? (onChange = callback, my) : onChange;
  }

  return my;
}

const Hierarchy = React.createClass({
  componentDidMount: function() {
    initHierarchy();
  },

  showFilters: function() {
    $('#filter-wrapper').toggle();
  },

  render: function() {
    return (
      <Panel title={t(title)}>
        <button id="toggle-svg-filter" className="btn btn-default" onClick={this.showFilters}>{t('Toggle filters')}</button>
        <div id="svg-wrapper">
          <div id="filter-wrapper"></div>
          <div className="detailBox"></div>
        </div>
      </Panel>
    );
  }
});

ReactDOM.render(
  <Hierarchy />,
  document.getElementById('hierarchy')
);
