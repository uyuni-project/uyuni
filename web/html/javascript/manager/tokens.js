'use strict';

$(function() {
  $('#channel-list').select2()
  $('#orgaccess').prop('checked', true);

  $('#orgaccess').change(function() {
    $('#channels-group').prop('disabled', $('#orgaccess').is(':checked'));
  });

});
