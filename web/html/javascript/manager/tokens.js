'use strict';

$(function() {
  new Clipboard('button#copy-token');
  $('#error').hide();
  $('#orgaccess').prop('checked', true);
  $('#channel-list').attr('disabled', true);

  $('#orgaccess').change(function() {
    $('#channel-list').attr('disabled', $('#orgaccess').is(':checked'));
  });

  $('#channel-list').select2()

  $('button#create').click(function() {
     $('#error').hide();
     var params = {};
     if ($('#orgaccess').is(':checked')) {
       var orgid = $('#orgid').val();
       params['orgid'] = orgid;
     } else {
       var channels = $('#channel-list').val() || [];
       if (channels.length > 0) {
         params['channels'] = encodeURIComponent(channels.join(','));
       }
     }

     var xhr = $.get('/rhn/manager/api/tokens/create', params)
     xhr.done(function(data) {
       $('input#token').val(data);
     })
     xhr.fail(function() {
       $('#error').show();
       $('input#token').val('');
     });
  });
});
