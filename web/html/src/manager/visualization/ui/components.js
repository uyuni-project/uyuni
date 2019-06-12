/* eslint-disable */
'use strict';

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
    .append('span')
    .attr('class', 'filter-title')
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
// - applyCallback - applyCallback receiving picked time and date after user clicks
// the 'Apply' button
// - clearCallback - clearCallback will be executed when the user clicks the "Clear" button
function addCheckinTimePartitioningSelect(anchorId, applyCallback, clearCallback) {
  const checkinTimePartitioning = d3.select(anchorId)
    .append('div').attr('class', 'filter');

  checkinTimePartitioning
    .append('div')
    .attr('class', 'filter-title')
    .text(t('Partition systems by given check-in time:'));

  const inputGroup = checkinTimePartitioning
    .append('div')
    .attr('class', 'input-group');

  inputGroup
    .append('input')
    .attr('type', 'text')
    .attr('class', 'partitioning-datepicker input-group-addon');

  inputGroup
    .append('input')
    .attr('type', 'text')
    .attr('class', 'partitioning-timepicker input-group-addon');

  $(anchorId + ' .partitioning-datepicker').datepicker({
    autoclose: true,
    format: 'yyyy-mm-dd'
  });
  $(anchorId + ' .partitioning-datepicker').datepicker('setDate', new Date());
  $(anchorId + ' .partitioning-timepicker').timepicker({timeFormat: 'H:i:s', maxTime: '23:30:00'});
  $(anchorId + ' .partitioning-timepicker').timepicker('setTime', new Date());

  const checkinPartitioningButtons = checkinTimePartitioning.append('div').attr('class', 'btn-group');
  addButton(checkinPartitioningButtons, 'Apply', () => {
    const date = $(anchorId + ' .partitioning-datepicker' ).datepicker( 'getDate' );
    const time = $(anchorId + ' .partitioning-timepicker' ).timepicker( 'getTime' );
    const datetime = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
      time.getHours(), time.getMinutes(), time.getSeconds());
      applyCallback(datetime);
  });
  addButton(checkinPartitioningButtons, 'Clear', () => {
    clearCallback();
  });

  return checkinTimePartitioning;
}

// Add a checkbox to given selection
//
// params:
// - targetSelection - selection where to append the element to
// - caption - text label
// - icon - the fontawesome icon that is prepended to the caption
// - classes - classes of the caption (css styling)
// - callback - callback receiving the new value of checkbox when checkbox
// changes
function addCheckbox(targetSelection, caption, icon, classes, callback) {
  const parentDiv = targetSelection
    .append('div');
  const id = 'visualization-checkbox-' + caption.trim();

  parentDiv
    .append('input')
    .attr('type', 'checkbox')
    .attr('id', id)
    .on('change', function() { callback(this.checked); });

  parentDiv
    .append('label')
    .attr('for', id)
    .classed(classes, true)
    .html((icon ?'<i class="fa ' + icon + '"></i>' : '') + caption);
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
  const NO_GROUP_LABEL = '** NO GROUP **';
  const data = [];
  let onChange = function(data) { console.log('data changed: ' + data); };
  groups = Array.from(new Set(groups));

  function appendAdder() {
    element
      .append('a')
      .attr('href', '#')
      .attr('class', 'toggle-grouping-level')
      .text(t('Add a grouping level'))
      .on('click', d => {
        data.push([]);
        update();
      })
      .append('i')
      .attr('class', 'fa fa-plus');
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
      .attr('class', 'apply-select2js-on-this')
      .attr('multiple', 'multiple')
      .attr('style', 'width: 250px');

    selectEnter
      .selectAll('option')
      .data(groups)
      .enter()
      .append('option')
      .attr('value', d => d)
      .text(d => d);

    selectEnter
      .append('option')
      .attr('value', NO_GROUP_LABEL)
      .text(NO_GROUP_LABEL);

    //HACK: usage of JQuery here is needed to apply the select2js plugin
    $('select.apply-select2js-on-this').each(function(i) {
      var select = $(this);
      // apply select2js only one time
      if (!select.hasClass('select2js-applied')) {
        select.addClass('select2js-applied');
        var select2js = select.select2({placeholder: t('Select a system group')});
        select2js.on("change", function(event) {
          data[i] = select.val() || [];
          onChange(data);
        });

        divEnter
          .append('a')
          .attr('href', '#')
          .attr('class', 'toggle-grouping-level')
          .attr('title', 'Remove this level')
          .on('click', function() {
            data.splice(i, 1);
            onChange(data);
            update();
          })
          .append('i')
          .attr('class', 'fa fa-close');
      }
      else {
        // if data is changed, e.g. a level is removed,
        // we need to keep aligned data and selected values
        if(data[i] != null && data[i].length > 0) {
          select.val(data[i]).trigger('change');
        }
      }
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
    .append('div')
    .attr('class', 'filter-title')
    .text(t('Split into groups'));

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
    .append('button')
    .attr('type', 'button')
    .attr('class', 'btn btn-default btn-sm')
    .on('click', callback)
    .text(caption);
}

function svgTextStyle(container) {
  const textStyle = container
    .append('defs')
    .append('filter')
    .attr('x', '0')
    .attr('y', '0')
    .attr('width', '1')
    .attr('height', '1')
    .attr('id', 'textStyle');
  textStyle
    .append('feFlood')
    .attr('flood-color', 'rgba(220, 220, 220, 0.8)');
  textStyle
    .append('feComposite')
    .attr('in', 'SourceGraphic');
}

module.exports = {
  addFilter: addFilter,
  addCheckinTimePartitioningSelect: addCheckinTimePartitioningSelect,
  addCheckbox: addCheckbox,
  addGroupSelector: addGroupSelector,
  addButton: addButton,
  svgTextStyle: svgTextStyle
}
