"use strict";

// D3 UI "components" used for visualization

// Add a filter to given selection
// params:
// - targetSelection - selection where to append the element to
// - caption - text label
// - placeholder - placeholder ("the example input text")a
// - onInputCallback - callback receiving the new value of input when input
// changes
function addFilter(targetSelection, caption, placeholder, onInputCallback) {
  const filterDiv = targetSelection
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

// Add a checkin-time partitioning selector (date- and timepicker + 'Apply'
// button)
//
// params:
// - anchorId - id of element in DOM where the UI will be added
// - callback - callback receiving picked time and date after user clicks
// the 'Apply' button
function addCheckinTimePartitioningSelect(anchorId, callback) {
  const checkinTimePartitioning = d3.select(anchorId)
    .append('div').attr('class', 'filter');

  checkinTimePartitioning
    .append('label')
    .text('Partition systems by given check-in time:');

  checkinTimePartitioning
    .append('input')
    .attr('class', 'partitioning-datepicker')
    .attr('type', 'text');

  checkinTimePartitioning
    .append('input')
    .attr('class', 'partitioning-timepicker')
    .attr('type', 'text');

  $(anchorId + ' .partitioning-datepicker').datepicker({
    autoclose: true,
    format: 'yyyy-mm-dd'
  });
  $(anchorId + ' .partitioning-datepicker').datepicker('setDate', new Date());
  $(anchorId + ' .partitioning-timepicker').timepicker({timeFormat: 'H:i:s'});
  $(anchorId + ' .partitioning-timepicker').timepicker('setTime', new Date());

  addButton(checkinTimePartitioning, 'Apply', () => {
      const date = $(anchorId + ' .partitioning-datepicker' ).datepicker( "getDate" );
      const time = $(anchorId + ' .partitioning-timepicker' ).timepicker( "getTime" );
      const datetime = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
        time.getHours(), time.getMinutes(), time.getSeconds());
      callback(datetime);
    });
}

// Add a checkbox to given selection
//
// params:
// - targetSelection - selection where to append the element to
// - caption - text label
// - callback - callback receiving the new value of checkbox when checkbox
// changes
function addCheckbox(targetSelection, caption, callback) {
  const parentDiv = targetSelection
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
//  - onChange: setter/getter for a function that is called after a selection
//  is changed or a select box on one level is collapsed
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

// Add a group selector (see comment for groupSelector)
//
// params:
// - targetSelection - selection where to append the element to
// - groups - all possible groups to appear in the select boxes
// - callback - function called after the state of UI changes (either user
// select a group from the selectbox or they remove the select box for a
// grouping level)
function addGroupSelector(targetSelection, groups, callback) {
  const groupingDiv = targetSelection
    .append('div').attr('class', 'filter');
  groupingDiv
    .append('label')
    .text('Split into groups');

  let mySel = groupSelector(groups, groupingDiv);
  mySel.onChange(callback);
  mySel();
}

// Add a button with a caption
//
// params:
// - targetSelection - selection where to append the element to
// - caption
// - callback - called after the button is clicked
function addButton(targetSelection, caption, callback) {
  targetSelection
    .append('div').attr('class', 'filter')
    .append('button')
    .attr('type', 'button')
    .attr('class', 'btn btn-default')
    .on('click', callback)
  .text(caption);
}

module.exports = {
  addFilter: addFilter,
  addCheckinTimePartitioningSelect: addCheckinTimePartitioningSelect,
  addCheckbox: addCheckbox,
  addGroupSelector: addGroupSelector,
  addButton: addButton
}

