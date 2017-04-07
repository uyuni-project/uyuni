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

  checkinTimeCriteria
    .append('button')
    .attr('type', 'button')
    .attr('class', 'btn btn-default')
    .on('click', function() {
      const date = $(anchorId + ' .criteria-datepicker' ).datepicker( "getDate" );
      const time = $(anchorId + ' .criteria-timepicker' ).timepicker( "getTime" );
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

