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
    .on('click', function() {
      const date = $( '#criteria-datepicker' ).datepicker( "getDate" );
      const time = $( '#criteria-timepicker' ).timepicker( "getTime" );
      const datetime = new Date(date.getFullYear(), date.getMonth(), date.getDate(),
        time.getHours(), time.getMinutes(), time.getSeconds());
      callback(datetime);
    })
    .text('Apply');
}

module.exports = {
  addFilter: addFilter,
  addCheckinTimeCriteriaSelect: addCheckinTimeCriteriaSelect
}

