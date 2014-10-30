
$(function() {
  var button = $('#scc-start-migration-btn');
  var dialog = $('#scc-migration-dialog');
  var dialogCloseBtn = $('#scc-migrate-dialog-close-btn');
  var statusLabel = $('#scc-migration-dialog-status');

  var completedTasks = 0;
  var totalTasks = 6;

  function addLastTask(message, iconClass) {
    var icon = $('<i></i>').attr('class', iconClass);
    var iconText = $('<div></div>');
    iconText.append(icon);
    iconText.append(message);
    $('#scc-migration-current-task').html(iconText);
  }

  function addTask(message) {
    addLastTask(message, 'fa fa-spinner fa-spin');
  }

  function taskFailed(message) {
    addLastTask(message, 'fa fa-exclamation-triangle fa-1-5x text-warning');
  }

  function taskSucceeded(message) {
    addLastTask(message, 'fa fa-check success');
    completedTasks = completedTasks + 1;
    statusLabel.html(completedTasks + '/' + totalTasks);
  }

  // starts a task func with message. On completion
  // continues with task then
  function startTask(message, taskFunc, thenFunc, cleanupFunc) {
    addTask(message);
    taskFunc(makeAjaxHandler(
      function(ret) {
        taskSucceeded(message);
        thenFunc();
      },
      function(message, exception) {
        taskFailed(message);
        cleanupFunc();
      })
    );
  }

  button.click(function() {
    button.prop('disabled', true);
    dialog.modal({show: true, keyboard: false});
    dialog.modal('show');
    // reload the page after the dialog is closed
    dialog.on('hidden.bs.modal', function() {
      location.reload();
    });

    dialogCloseBtn.prop('disabled', true);

    function cleanupFunc() {
        dialogCloseBtn.prop('disabled', false);
        statusLabel.html(
          '<i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>' +
          $('#sccconfig\\.jsp\\.failed').html());
    }

    statusLabel.html('0/6');
    // as one tasks succeeeds, start the next
    startTask($('#sccconfig\\.jsp\\.switchingtoscc').html(), SCCConfigAjax.performMigration, function() {
      startTask($('#sccconfig\\.jsp\\.channels').html(), SCCConfigAjax.synchronizeChannels, function() {
          startTask($('#sccconfig\\.jsp\\.channelfamilies').html(), SCCConfigAjax.synchronizeChannelFamilies, function() {
            startTask($('#sccconfig\\.jsp\\.products').html(), SCCConfigAjax.synchronizeProducts, function() {
              startTask($('#sccconfig\\.jsp\\.productchannels').html(), SCCConfigAjax.synchronizeProductChannels, function() {
                startTask($('#sccconfig\\.jsp\\.subscriptions').html(), SCCConfigAjax.synchronizeSubscriptions, function() {
                startTask($('#sccconfig\\.jsp\\.upgradepaths').html(), SCCConfigAjax.synchronizeUpgradePaths, function() {
                  button.hide();
                  button.prop('disabled', false);
                  dialogCloseBtn.prop('disabled', false);
                  statusLabel.html('<i class="fa fa-check success"></i>' + $('#sccconfig\\.jsp\\.completed').html());
                }, cleanupFunc);
              }, cleanupFunc);
            }, cleanupFunc);
          }, cleanupFunc);
        }, cleanupFunc);
      }, cleanupFunc);
      }, cleanupFunc);
    });
});
