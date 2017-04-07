"use strict";

const Network = require("../../utils/network");
const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../../components/panel").Panel;
const HierarchyView = require("./hierarchy-view.js");
const Preprocessing = require("./preprocessing.js");
const UI = require("./ui.js");
const Utils = require("./utils.js");

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
        const container = Utils.prepareDom();
        const tree = HierarchyView.customTree(d, container);
        if (view == 'grouping') { // hack - derive preprocessor from global variable
          tree.preprocessor(Preprocessing.grouping());
        }
        tree.refresh();

        UI.addFilter('#filter-wrapper', 'Filter by system name', 'e.g., client.nue.sles', (input) => {
          tree.filters().put('name', d => d.data.name.toLowerCase().includes(input.toLowerCase()));
          tree.refresh();
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
              tree.filters().remove('patch_count_filter');
            } else {
              tree.filters().put('patch_count_filter', d => {
                return HierarchyView.isSystemType(d) &&
                  patchCountFilterConfig // based on the checkboxes state, take into account the patch count
                    .map((value, index) => value && (d.data.patch_counts || [])[index] > 0)
                    .reduce((a, b) => a || b, false);
              });
            }
            tree.refresh();
          }
        }
        appendCheckbox(patchCountsFilter, 'has bug fix advisories', patchCountFilterCallback(0));
        appendCheckbox(patchCountsFilter, 'OR has product enhancement advisories', patchCountFilterCallback(1));
        appendCheckbox(patchCountsFilter, 'OR has security advisories', patchCountFilterCallback(2));

        UI.addFilter('#filter-wrapper', 'Filter by system base channel', 'e.g., SLE12', (input) => {
          tree.filters().put('base_channel', d => (d.data.base_channel || '').toLowerCase().includes(input.toLowerCase()));
          tree.refresh();
        });

        UI.addFilter('#filter-wrapper', 'Filter by system installed products', 'e.g., SLES', (input) => {
          tree.filters().put('installedProducts', d =>  (d.data.installedProducts || []).map(ip => ip.toLowerCase().includes(input.toLowerCase())).reduce((v1,v2) => v1 || v2, false));
          tree.refresh();
        });

        if (tree.preprocessor().groupingConfiguration) { // we have a processor responding to groupingConfiguration
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
            tree.preprocessor().groupingConfiguration(data);
            tree.refresh();
          });
          mySel();
        }

        function updateTree() {
          const date = $( '#criteria-datepicker' ).datepicker( "getDate" );
          const time = $( '#criteria-timepicker' ).timepicker( "getTime" );

          const dateTime = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
            time.getHours(), time.getMinutes(), time.getSeconds());
          tree.criteria().get()['user-criteria'] = d => {
            if (d.data.checkin == undefined) {
              return '';
            }
            var firstPartition = d.data.checkin < dateTime.getTime();
            d.data.partition = firstPartition;
            return firstPartition  ? 'stroke-red' : 'stroke-green';
          };
          tree.refresh();
        }

        function resetTree() {
          tree.criteria().get()['user-criteria'] = d => { return ''};
          tree.refresh();
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
          tree.criteria().get()['user-criteria'] = d => {
            if (d.data.patch_counts == undefined) {
              return '';
            }
            var firstPartition = d.data.patch_counts.filter(pc => pc > 0).length > 0;
            d.data.partition = firstPartition;
            return firstPartition  ? 'stroke-red' : 'stroke-green';
          };
          tree.refresh();
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
          const dimensions = Utils.computeSvgDimensions();
          // try to find the object via d3
          d3.select('#svg-wrapper svg')
            .attr('width', dimensions[0])
            .attr('height', dimensions[1]);
        });

        function addVisibleTreeToSSM() {
          const ids = new Set();
          tree.view().root().each(e => {
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
