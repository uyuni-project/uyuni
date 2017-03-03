"use strict";

const Network = require("../../utils/network");
const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../../components/panel").Panel;
const HierarchyView = require("./hierarchy-view.js");
const Filters = require("./filters.js");
const Criteria = require("./criteria.js");

$(document).ready(function() {
  // disable the #spacewalk-content observer:
  // drawing svg triggers it for every small changes,
  // and it is not the desired behaviour/what the observer stands for
  // note: leaving it connected slow down the svg usability
  spacewalkContentObserver.disconnect();

  var mainDivWidth = d3.select('#svg-wrapper').node().getBoundingClientRect().width - 2;
  var mainDivHeight = d3.select('.spacewalk-main-column-layout').node().getBoundingClientRect().height - 2 -
      d3.select('#breadcrumb').node().getBoundingClientRect().height -
      d3.select('section .spacewalk-toolbar-h1').node().getBoundingClientRect().height;

  // Get data & put everything together in the graph!
  Network
    .get(endpoint, "application/json")
    .promise
    .then(d => {
      var root = d3.stratify()(d);
      root.x = mainDivWidth / 2;
      root.y = mainDivHeight / 2;
      var tree = d3.tree().size([mainDivWidth, mainDivHeight]);
      tree(root);
      root.each(d => {
        d._allChildren = d.children;  // backup children
      });

      // Prepare DOM
      var svg = d3.select('#svg-wrapper')
        .append('svg')
        .attr('width', mainDivWidth)
        .attr('height', mainDivHeight);
      var container = svg.append("g")
        .attr("transform", "translate(" + mainDivWidth / 2 + ',' + mainDivHeight / 2 + ")");

      // Zoom handling
      activateZoom(svg, container);
      function activateZoom(svg, elem) {
        svg.call(d3.zoom()
          .scaleExtent([1 / 8, 16])
          .on("zoom", zoomed))
          .on("dblclick.zoom", null);

        function zoomed(d) {
          var event = d3.event;
          elem.attr("transform", event.transform);
        }
      }

    // Returns a value bound to the depth level of the node
    function distanceFromDepth(depth) {
      switch (depth) {
        case 0: return 200;
        case 1: return 80;
        default: return 30;
      }
    }

    // Prepare simulation
    var mySimulation = d3.forceSimulation()
      .force("charge", d3.forceManyBody().strength(d => -distanceFromDepth(d.depth) * 1.5))
      .force("link", d3.forceLink().distance(d =>distanceFromDepth(d.source.depth)))
      .force("x", d3.forceX())
      .force("y", d3.forceY());

    // Returns the CSS class for the given node
    // simple algorithm based on depth
    var myDeriveClass = function(node) {
      switch (node.depth) {
          case 0: return 'depth0';
          case 1: return 'depth1';
          default: return 'default';
      }
    }

    var t = HierarchyView.hierarchyView(root, container)
      .simulation(mySimulation)
      .deriveClass(myDeriveClass);
    t();

    function nodeVisible(node, pred) {
      // dfs
      if (!node.children) {
        return pred(node);
      }

      const visibleChildren = node._allChildren.filter(c => nodeVisible(c, pred));
      node.children = visibleChildren;
      return visibleChildren.length > 0;
    }

    // Prepare filters
    const myFilters = Filters.filters();
    const myCriteria = Criteria.criteria();
    myCriteria.get()['default'] = myDeriveClass;

    const nameFilterDiv = d3.select('#filter-wrapper')
      .append('div').attr('class', 'filter');
    nameFilterDiv
      .append('label')
      .text('Entity name');
    nameFilterDiv
      .append('input')
      .attr('type', 'text')
      .attr('placeholder', 'e.g., client.nue.sles')
      .on('input', function() {
        myFilters.put('name', d => d.data.name.toLowerCase().includes(this.value.toLowerCase()));
        nodeVisible(root, myFilters.predicate());
        t();
      });

    const baseProdFilterDiv = d3.select('#filter-wrapper')
      .append('div').attr('class', 'filter');
    baseProdFilterDiv
       .append('label')
       .text('Entity base channel');
    baseProdFilterDiv
      .append('input')
      .attr('type', 'text')
      .attr('placeholder', 'e.g., SLE12')
      .on('input', function() {
        myFilters.put('base_channel', d => (d.data.base_channel || '').toLowerCase().includes(this.value.toLowerCase()));
        nodeVisible(root, myFilters.predicate());
        t();
      });

      const checkinTimeCriteria = d3.select('#filter-wrapper')
        .append('div').attr('class', 'filter');
      checkinTimeCriteria
         .append('label')
         .text('Partition systems by given check-in time:');
      checkinTimeCriteria
        .append('input')
        .attr('type', 'text')
        .attr('value', new Date())
        .on('input', function() {
          myCriteria.get()['checkin-criteria'] = d => {
              var firstPartition = d.data.checkin < new Date(this.value).getTime();
              d.data.partition = firstPartition;
              return firstPartition  ? 'stroke-green' : 'stroke-red';
          };
          t.deriveClass(myCriteria.deriveClass)
          t();
        });
    });
});

const Hierarchy = React.createClass({
  componentDidMount: function() {
  },

  render: function() {
    return (
      <Panel title={t(title)}>
        <div className="svg-aside">
          <div id="filter-wrapper"></div>
          <div className="detailBox"></div>
        </div>
        <div id="svg-wrapper"></div>
      </Panel>
    );
  }
});

ReactDOM.render(
  <Hierarchy />,
  document.getElementById('hierarchy')
);
