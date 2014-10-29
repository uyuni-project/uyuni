
$(function() {
  var button = $('#scc-start-migration-btn');
  var inProgressMsg = $('#scc-migration-in-progress-msg');
  var completedMsg = $('#scc-migration-in-completed-msg');
  var failedMsg = $('#scc-migration-in-failed-msg');

  inProgressMsg.hide();
  completedMsg.hide();
  failedMsg.hide();

  button.click(function() {

    button.prop('disabled', true);
    inProgressMsg.show();

    var ret = SCCConfigAjax.sayHello(makeAjaxHandler(
      function(ret) {
        inProgressMsg.hide();
        failedMsg.hide();
        completedMsg.show();

        $('#still-ncc-msg').hide();
        button.prop('disabled', false);

        button.hide();
      },
      function(message, exception) {
        inProgressMsg.hide();
        completedMsg.hide();
        failedMsg.show();

        $('#scc-migration-failed-details').html(message);

        button.prop('disabled', false);
      }
    ));
  });
});
