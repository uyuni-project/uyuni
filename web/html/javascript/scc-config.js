$(function() {
  // Helper function: show a given message with an icon
  function showMessageWithIcon(message, iconClass) {
    var icon = $('<i></i>').attr('class', iconClass);
    var iconText = $('<div></div>');
    iconText.append(icon);
    iconText.append(message);
    $('#scc-migration-current-task').html(iconText);
  }

  // Helper function: refresh the status label
  function refreshStatusLabel(completedTasks, totalTasks) {
    var statusLabel = $('#scc-migration-dialog-status');
    statusLabel.html(completedTasks + '/' + totalTasks);
  }

  // Central function to run a given list of tasks
  function runTasks(tasks, cleanupTask, currentTask, totalTasks) {
    if (tasks.length >= 1) {
      var message = $(tasks[0].messageKey).html();
      showMessageWithIcon(message, 'fa fa-spinner fa-spin');

      if (currentTask <= totalTasks) {
        refreshStatusLabel(currentTask, totalTasks);
      }

      tasks[0].task(makeAjaxHandler(
        function(ret) {
          showMessageWithIcon(message, 'fa fa-check success');
          runTasks(tasks.slice(1), cleanupTask, currentTask + 1, totalTasks);
        },
        function(message, exception) {
          showMessageWithIcon(message, 'fa fa-exclamation-triangle fa-1-5x text-warning');
          cleanupTask();
        })
      );
    }
  }

  // Show the dialog and perform migration and/or refresh
  function refreshContent(title, migration, refresh, onSuccess) {
    // Prepare the dialog and show
    $("#scc-refresh-dialog-title").html(title);
    var dialogCloseBtn = $('#scc-migrate-dialog-close-btn');
    dialogCloseBtn.prop('disabled', true);
    var dialog = $('#scc-migration-dialog');
    dialog.modal({show: true, keyboard: false});
    dialog.modal('show');

    // Reload the page after the dialog is closed
    dialog.on('hidden.bs.modal', function() {
      location.reload();
    });

    // A task to perform in case of success
    function successTask() {
      dialogCloseBtn.prop('disabled', false);
      showMessageWithIcon($('#sccconfig\\.jsp\\.completed').html(), "fa fa-check success");
      onSuccess();
    }

    // Compile the list of tasks to be executed
    tasks = [];
    if (migration) {
      tasks.push({"task" : SCCConfigAjax.performMigration, "messageKey" : "#sccconfig\\.jsp\\.switchingtoscc"});
    }
    if (refresh) {
      tasks.push({"task" : SCCConfigAjax.synchronizeChannels, "messageKey" : "#sccconfig\\.jsp\\.channels"});
      tasks.push({"task" : SCCConfigAjax.synchronizeChannelFamilies, "messageKey" : "#sccconfig\\.jsp\\.channelfamilies"});
      tasks.push({"task" : SCCConfigAjax.synchronizeProducts, "messageKey" : "#sccconfig\\.jsp\\.products"});
      tasks.push({"task" : SCCConfigAjax.synchronizeProductChannels, "messageKey" : "#sccconfig\\.jsp\\.productchannels"});
      tasks.push({"task" : SCCConfigAjax.synchronizeSubscriptions, "messageKey" : "#sccconfig\\.jsp\\.subscriptions"});
      tasks.push({"task" : SCCConfigAjax.synchronizeUpgradePaths, "messageKey" : "#sccconfig\\.jsp\\.upgradepaths"});
    }
    tasks.push({"task" : successTask, "messageKey" : "#sccconfig\\.jsp\\.completed"});

    // Cleanup task
    function cleanupTask() {
      dialogCloseBtn.prop('disabled', false);
      $('#scc-migration-dialog-status').html(
          '<i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>' +
      $('#sccconfig\\.jsp\\.failed').html());
    }

    // Run the list of tasks
    runTasks(tasks, cleanupTask, 1, tasks.length - 1);
  }

  // Event handler for button click event
  function onMigrationButtonClick() {
    var button = $('#scc-start-migration-btn');
    button.prop('disabled', true);
    refreshContent($("#sccconfig\\.jsp\\.switchingtoscc").html(), true, true, function() {
      button.hide();
      button.prop('disabled', false);
    });
  }

  // Event Handlers
  $("#scc-start-migration-btn").on("click", onMigrationButtonClick);
});

