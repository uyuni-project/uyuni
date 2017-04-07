"use strict";

// D3 UI "components" used for visualization

function addFilter(anchorId, caption, placeholder, onInputCallback) {
  const filterDiv = d3.select(anchorId)
    .append('div').attr('class', 'filter');
  filterDiv
    .append('label')
    .text(caption);
  filterDiv
    .append('input')
    .attr('type', 'text')
    .attr('placeholder', placeholder)
    .on('input', function() { onInputCallback(this.value) });
};

function addCheckinTimeCriteriaSelect(anchorId, callback) {
  const checkinTimeCriteria = d3.select(anchorId)
    .append('div').attr('class', 'filter');

  checkinTimeCriteria
    .append('label')
    .text('Partition systems by given check-in time:');

  checkinTimeCriteria
    .append('input')
    .attr('class', 'criteria-datepicker')
    .attr('type', 'text');

  checkinTimeCriteria
    .append('input')
    .attr('class', 'criteria-timepicker')
    .attr('type', 'text');

  $(anchorId + ' .criteria-datepicker').datepicker({
    autoclose: true,
    format: 'yyyy-mm-dd'
  });
  $(anchorId + ' .criteria-datepicker').datepicker('setDate', new Date());
  $(anchorId + ' .criteria-timepicker').timepicker({timeFormat: 'H:i:s'});
  $(anchorId + ' .criteria-timepicker').timepicker('setTime', new Date());

  addButton(checkinTimeCriteria, 'Apply', () => {
      const date = $(anchorId + ' .criteria-datepicker' ).datepicker( "getDate" );
      const time = $(anchorId + ' .criteria-timepicker' ).timepicker( "getTime" );
      const datetime = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
        time.getHours(), time.getMinutes(), time.getSeconds());
      callback(datetime);
    });
}

function addCheckbox(placeholder, caption, callback) {
  const parentDiv = placeholder
    .append('div');

  parentDiv
    .append('input')
    .attr('type', 'checkbox')
    .on('change', function() { callback(this.checked); });

  parentDiv
    .append('label')
    .text(caption);
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

function addGroupSelector(anchorId, groups, callback) {
  const groupingDiv = d3.select(anchorId)
    .append('div').attr('class', 'filter');
  groupingDiv
    .append('label')
    .text('Split into groups');

  let mySel = groupSelector(groups, groupingDiv);
  mySel.onChange(callback);
  mySel();
}

function addButton(anchorSelection, caption, callback) {
  anchorSelection
    .append('div').attr('class', 'filter')
    .append('button')
    .attr('type', 'button')
    .attr('class', 'btn btn-default')
    .on('click', callback)
  .text(caption);
}

module.exports = {
  addFilter: addFilter,
  addCheckinTimeCriteriaSelect: addCheckinTimeCriteriaSelect,
  addCheckbox: addCheckbox, // todo rename!
  addGroupSelector: addGroupSelector,
  addButton: addButton
}

