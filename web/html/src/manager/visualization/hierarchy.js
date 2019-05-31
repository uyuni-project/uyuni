/* eslint-disable */
'use strict';

const SpaRenderer  = require("core/spa/spa-renderer").default;
const Network = require('../../utils/network');
const React = require('react');
const ReactDOM = require('react-dom');
const { TopPanel } = require('components/panels/TopPanel');
const DataTree = require('./data-tree.js');
const Preprocessing = require('./data-processing/preprocessing.js');
const UI = require('./ui/components.js');
const Utils = require('./utils.js');

function displayHierarchy(data) {
  // disable the #spacewalk-content observer:
  // drawing svg triggers it for every small changes,
  // and it is not the desired behaviour/what the observer stands for
  // note: leaving it connected slow down the svg usability
  spacewalkContentObserver.disconnect();

  const container = Utils.prepareDom();
  const tree = DataTree.dataTree(data, container);
  if (view == 'grouping') { // hack - derive preprocessor from global variable
    tree.preprocessor(Preprocessing.grouping());
  }
  tree.refresh();

  initUI(tree);

  d3.select(window).on('resize', function () {
    Utils.adjustSvgDimensions();
  });
}

function showFilterTab(tabIdToShow) {
  d3.selectAll('.filter-tab-selector').classed('active', false);
  d3.selectAll('.filter-tab').classed('active', false);
  d3.select('#' + tabIdToShow + '-selector').classed('active', true);
  d3.select('#' + tabIdToShow).classed('active', true);
  Utils.adjustSvgDimensions();
}

// util function for adding the UI to the dom and setting its callbacks
function initUI(tree) {
  const filterNavTab = d3.select('#visualization-filter-wrapper')
    .append('ul')
    .attr('class', 'nav nav-tabs');

  filterNavTab
    .append('li')
    .attr('id', 'filtering-tab-selector')
    .attr('class', 'filter-tab-selector active')
    .append('a')
    .text(t('Filtering'))
    .on('click', d => {
      showFilterTab('filtering-tab');
    });
  filterNavTab
    .append('li')
    .attr('id', 'partitioning-tab-selector')
    .attr('class', 'filter-tab-selector')
    .append('a')
    .text(t('Partitioning'))
    .on('click', d => {
      showFilterTab('partitioning-tab');
    });

  d3.select('#visualization-filter-wrapper')
    .append('div')
    .attr('id', 'filtering-tab')
    .attr('class', 'filter-tab active');
  d3.select('#visualization-filter-wrapper')
    .append('div')
    .attr('id', 'partitioning-tab')
    .attr('class', 'filter-tab');

  // Patch count filter
  const patchCountsFilter = d3.select('#filtering-tab')
    .append('div').attr('class', 'filter');

  patchCountsFilter
    .append('div')
    .attr('class', 'filter-title no-bold')
    .text(t('Show systems with:'));

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
          return Utils.isSystemType(d) &&
          patchCountFilterConfig // based on the checkboxes state, take into account the patch count
          .map((value, index) => value && (d.data.patch_counts || [])[index] > 0)
          .reduce((a, b) => a || b, false);
        });
      }
      tree.refresh();
    }
  }
  UI.addCheckbox(patchCountsFilter, t('security advisories'), 'fa-shield', 'security-patches', patchCountFilterCallback(2));
  UI.addCheckbox(patchCountsFilter, t('bug fix advisories'), 'fa-bug', 'bug-patches', patchCountFilterCallback(0));
  UI.addCheckbox(patchCountsFilter, t('product enhancement advisories'), 'spacewalk-icon-enhancement', 'minor-patches', patchCountFilterCallback(1));

  d3.select('#filtering-tab').append('div').attr('id', 'filter-systems-box');
  // System name filter
  UI.addFilter(d3.select('#filter-systems-box'), t('Filter by system name'), t('e.g., client.nue.sles'), (input) => {
    tree.filters().put('name', d => d.data.name.toLowerCase().includes(input.toLowerCase()));
    tree.refresh();
  });

  // Base channel filter
  UI.addFilter(d3.select('#filter-systems-box'), t('Filter by system base channel'), t('e.g., SLE12'), (input) => {
    tree.filters().put('base_channel', d => (d.data.base_channel || '').toLowerCase().includes(input.toLowerCase()));
    tree.refresh();
  });

  // Installed products filter
  UI.addFilter(d3.select('#filter-systems-box'), t('Filter by system installed products'), t('e.g., SLES'), (input) => {
    if (input == undefined || input == '') {
      tree.filters().remove('installedProducts');
    } else {
      tree.filters().put('installedProducts', d => (d.data.installedProducts || [])
        .map(ip => ip.toLowerCase().includes(input.toLowerCase()))
        .reduce((v1,v2) => v1 || v2, false));
    }
    tree.refresh();
  });


  // Partitioning by checkin time
  function partitionByCheckin(datetime) {
    tree.partitioning().get()['user-partitioning'] = d => {
      if (d.data.checkin == undefined) {
        return '';
      }
      const firstPartition = d.data.checkin < datetime.getTime();
      d.data.partition = firstPartition;
      return firstPartition  ? 'stroke-red non-checking-in' : 'stroke-green checking-in';
    };
    tree.refresh();
  }
  function clearPartitioning() {
    tree.partitioning().get()['user-partitioning'] = d => { return ''};
    tree.refresh();
  }

  UI.addCheckinTimePartitioningSelect('#partitioning-tab', partitionByCheckin, clearPartitioning);

  // Partitioning by patch existence
  const hasPatchesPartitioning = d3.select('#partitioning-tab')
    .append('div').attr('class', 'filter');

  hasPatchesPartitioning
    .append('div')
    .attr('class', 'filter-title')
    .text(t('Partition systems based on whether there are patches for them:'));

  function applyPatchesPartitioning() {
    tree.partitioning().get()['user-partitioning'] = d => {
      if (!Utils.isSystemType(d) || d.data.patch_counts == undefined) {
        return '';
      }
      const firstPartition = d.data.patch_counts.filter(pc => pc > 0).length > 0;
      d.data.partition = firstPartition;
      return firstPartition ? 'stroke-red unpatched' : 'stroke-green patched';
    };
    tree.refresh();
  }

  const patchesPartitioningButtons = hasPatchesPartitioning.append('div').attr('class', 'btn-group');
  UI.addButton(patchesPartitioningButtons, 'Apply', applyPatchesPartitioning);
  UI.addButton(patchesPartitioningButtons, 'Clear', clearPartitioning);

  // Grouping UI (based on the preprocessor type)
  if (tree.preprocessor().groupingConfiguration) { // we have a processor responding to groupingConfiguration
    UI.addGroupSelector(d3.select('#partitioning-tab'),
        tree.data().map(e => e.managed_groups || []).reduce((a,b) => a.concat(b)),
        (data) => {
          tree.preprocessor().groupingConfiguration(data);
          tree.refresh();
        });
  }
}

class Hierarchy extends React.Component {
  state = { showFilters: false };

  componentDidMount() {
    // Get data & put everything together in the graph!
    Network
      .get(endpoint, 'application/json')
      .promise
      .then(
        (data) => $(document).ready(() => displayHierarchy(data)),
        (xhr) =>  d3.select('#svg-wrapper').text(t('There was an error fetching data from the server.'))
      );
  }

  showFilters = () => {
    const filterBox = $('#visualization-filter-wrapper');
    if (filterBox.hasClass("open")) {
      filterBox.removeClass('open').slideUp('fast', () => {Utils.adjustSvgDimensions()});
      this.setState({ showFilters: false});
    }
    else {
      filterBox.addClass('open').slideDown('fast', () => {Utils.adjustSvgDimensions()});
      this.setState({ showFilters: true});
    }
  };

  render() {
    var hurl = null;
    if (title === "Virtualization Hierarchy") {
      hurl = "/docs/reference/systems/virtualization-hierarchy.html";
    } else if(title === "Proxy Hierarchy") {
      hurl = "/docs/reference/systems/proxy-hierarchy.html";
    } else if(title === "Systems Grouping" ) {
      hurl = "/docs/reference/systems/systems-grouping.html";
    }

    return (
      <TopPanel title={t(title)} helpUrl={hurl}>
        <button className='toggle-filter-button' onClick={this.showFilters}>
          {t((this.state.showFilters ? 'Hide' : 'Show') + ' filters')}
          <i className={"fa fa-caret-" + (this.state.showFilters ? 'up' : 'down')} aria-hidden="true"></i>
        </button>
        <div id='visualization-filter-wrapper'></div>
        <div id='svg-wrapper'>
          <div className='detailBox'></div>
        </div>
      </TopPanel>
    );
  }
}

SpaRenderer.renderNavigationReact(
  <Hierarchy />,
  document.getElementById('hierarchy')
);
