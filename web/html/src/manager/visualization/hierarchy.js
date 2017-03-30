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

function initHierarchy() {
  $(document).ready(function() {
    // disable the #spacewalk-content observer:
    // drawing svg triggers it for every small changes,
    // and it is not the desired behaviour/what the observer stands for
    // note: leaving it connected slow down the svg usability
    spacewalkContentObserver.disconnect();

    var mainDivWidth = d3.select('#svg-wrapper').node().getBoundingClientRect().width - 2;
    var mainDivHeight = d3.select('.spacewalk-main-column-layout').node().getBoundingClientRect().height - 2 -
        d3.select('#breadcrumb').node().getBoundingClientRect().height -
        d3.select('section .spacewalk-toolbar-h1').node().getBoundingClientRect().height - 200;

    function adjustSvgDimensions(svgObject, simulation) {
      var mainWidth = d3.select('#svg-wrapper').node().getBoundingClientRect().width - 2;
      var mainHeight = d3.select('.spacewalk-main-column-layout').node().getBoundingClientRect().height - 2 -
        d3.select('#breadcrumb').node().getBoundingClientRect().height -
        d3.select('section .spacewalk-toolbar-h1').node().getBoundingClientRect().height - 200;

      if (svgObject) {
        svgObject
          .attr('width', mainWidth)
          .attr('height', mainHeight);
      }
      else {
        // try to find the object via d3
        d3.select('#svg-wrapper svg')
          .attr('width', mainWidth)
          .attr('height', mainHeight);
      }

      if (simulation) {
        simulation
         .force("x", d3.forceX(mainWidth / 2))
         .force("y", d3.forceY(mainHeight / 2));
     }
    }

    // Get data & put everything together in the graph!
    Network
      .get(endpoint, "application/json")
      .promise
      .then(d => {
        // Prepare DOM
        var svg = d3.select('#svg-wrapper')
          .append('svg')
          .attr('width', mainDivWidth)
          .attr('height', mainDivHeight);
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

        // Prepare simulation
        var mySimulation = d3.forceSimulation()
          .force("charge", d3.forceManyBody().strength(d => -distanceFromDepth(d.depth) * 1.5))
          .force("link", d3.forceLink().distance(d =>distanceFromDepth(d.source.depth)))
          .force("x", d3.forceX(mainDivWidth / 2))
          .force("y", d3.forceY(mainDivHeight / 2));

        // Returns the CSS class for the given node
        // simple algorithm based on depth
        var myDeriveClass = function(node) {
          switch (node.depth) {
              case 0: return 'depth0';
              case 1: return 'depth1';
              default: return 'default';
          }
        }

        let dataProcessor = Preprocessing.stratify(d);
        if (preprocessor == 'grouping') {
          dataProcessor = Preprocessing.grouping(d);
        }
        const root = dataProcessor();
        treeify(root, [mainDivWidth, mainDivHeight]);
        const t = HierarchyView.hierarchyView(root, container)
          .simulation(mySimulation)
          .deriveClass(myDeriveClass);
        t();

        // Returns a value bound to the depth level of the node
        function distanceFromDepth(depth) {
          switch (depth) {
            case 0: return 200;
            case 1: return 80;
            default: return 30;
          }
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

        // Prepare filters
        const myFilters = Filters.filters();
        const myCriteria = Criteria.criteria();
        myCriteria.get()['default'] = myDeriveClass;

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
            myFilters.put('name', d => d.data.name.toLowerCase().includes(this.value.toLowerCase()));
            refreshTree(dataProcessor, myFilters, myCriteria, t);
          });

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
            myFilters.put('base_channel', d => (d.data.base_channel || '').toLowerCase().includes(this.value.toLowerCase()));
            refreshTree(dataProcessor, myFilters, myCriteria, t);
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
            myFilters.put('installedProducts', d =>  (d.data.installedProducts || []).map(ip => ip.toLowerCase().includes(this.value.toLowerCase())).reduce((v1,v2) => v1 || v2, false));
            refreshTree(dataProcessor, myFilters, myCriteria, t);
          });

        function refreshTree(processor, filters, criteria, tree) {
          const newRoot = processor();
          treeify(newRoot, [mainDivWidth, mainDivHeight]);
          tree.root(newRoot);
          nodeVisible(newRoot, filters.predicate());
          tree.deriveClass(criteria.deriveClass)
          tree();
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
            refreshTree(dataProcessor, myFilters, myCriteria, t);
          });
          mySel();

        }

        function updateTree() {
          const date = $( '#criteria-datepicker' ).datepicker( "getDate" );
          const time = $( '#criteria-timepicker' ).timepicker( "getTime" );

          const dateTime = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
            time.getHours(), time.getMinutes(), time.getSeconds());
          myCriteria.get()['checkin-criteria'] = d => {
            var firstPartition = d.data.checkin < dateTime.getTime();
            d.data.partition = firstPartition;
            return firstPartition  ? 'stroke-red' : 'stroke-green';
          };
          refreshTree(dataProcessor, myFilters, myCriteria, t);
        }

        function resetTree() {
          myCriteria.get()['checkin-criteria'] = d => { return ''};
          t();
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
        checkinTimeCriteria
          .append('button')
          .attr('type', 'button')
          .attr('class', 'btn btn-default')
          .on('click', resetTree)
          .text('Reset');

        adjustSvgDimensions(svg, mySimulation);

        $(window).resize(function () {
          adjustSvgDimensions();
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
