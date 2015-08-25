$(function() {
  // Helper function: show a given message with an icon
  function showMessageWithIcon(message, iconClass) {
    var icon = $('<i></i>').attr('class', iconClass);
    var iconText = $('<div></div>');
    iconText.append(icon);
    iconText.append(message);
    $('#scc-migration-dialog-status').html(iconText);
  }

  function updateTaskMessageWithIcon(taskIndex, message, iconClass, textClass) {
    // Helper function to update a task item message and icon
    var taskItemSel = '#scc-task-list li[data-task="' + taskIndex + '"]';
    var li = $(taskItemSel)
    if (li.length < 1) {
      $('#scc-task-list').append('<li data-task="' + taskIndex + '"></li>');
      li = $(taskItemSel);
    }
    var icon = $('<i></i>').attr('class', iconClass);
    icon.addClass('fa fa-li');
    li.empty();
    li.append(icon);
    var textDiv = $('<div></div>');
    textDiv.html(message);
    textDiv.attr('class', textClass);
    li.append(textDiv);
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
      updateTaskMessageWithIcon(currentTask, message, 'fa-spinner fa-spin', '');

      if (currentTask <= totalTasks) {
        refreshStatusLabel(currentTask, totalTasks);
      }

      tasks[0].task(makeAjaxHandler(
        function(ret) {
          updateTaskMessageWithIcon(currentTask, message, 'fa-check text-success', 'text-success');
          runTasks(tasks.slice(1), cleanupTask, currentTask + 1, totalTasks);
        },
        function(errmsg, exception) {
          updateTaskMessageWithIcon(currentTask, message, 'fa-exclamation-triangle text-warning', 'text-warning');
          cleanupTask(errmsg, exception);
        })
      );
    }
  }

  // Show the dialog and perform migration and/or refresh
  function refreshContent(title, refresh, onSuccess) {
    // Prepare the dialog and show
    $("#scc-refresh-dialog-title").html(title);
    var dialogCloseBtn = $('#scc-migrate-dialog-close-btn');
    dialogCloseBtn.prop('disabled', true);
    var dialog = $('#scc-migration-dialog');
    dialog.modal({show: true, keyboard: false, backdrop: "static"});

    // Compile the list of tasks to be executed
    tasks = [];
    if (refresh) {
      tasks.push({"task" : MgrSyncAJAX.synchronizeChannels, "messageKey" : "#sccconfig\\.jsp\\.channels"});
      tasks.push({"task" : MgrSyncAJAX.synchronizeChannelFamilies, "messageKey" : "#sccconfig\\.jsp\\.channelfamilies"});
      tasks.push({"task" : MgrSyncAJAX.synchronizeProducts, "messageKey" : "#sccconfig\\.jsp\\.products"});
      tasks.push({"task" : MgrSyncAJAX.synchronizeProductChannels, "messageKey" : "#sccconfig\\.jsp\\.productchannels"});
      tasks.push({"task" : MgrSyncAJAX.synchronizeSubscriptions, "messageKey" : "#sccconfig\\.jsp\\.subscriptions"});
    }
    // A task to perform in case of success
    function successTask() {
      dialogCloseBtn.prop('disabled', false);
      var message = $('#sccconfig\\.jsp\\.completed').html();
      updateTaskMessageWithIcon(tasks.length, message, 'fa-check text-success', 'text-success');
      showMessageWithIcon(message, 'fa fa-check text-success');
      onSuccess();
    }
    tasks.push({"task" : successTask, "messageKey" : "#sccconfig\\.jsp\\.completed"});

    // Cleanup task
    function cleanupTask(errmsg, exception) {
      dialogCloseBtn.prop('disabled', false);
      var message = $('#sccconfig\\.jsp\\.failed').html();
      var link = $('#sccconfig\\.jsp\\.failed\\.details\\.link').html();
      showMessageWithIcon(
        message + ': ' + exception.message + ' (<a href="/rhn/admin/Catalina.do">' +
          link + '</a>)',
        'fa fa-exclamation-triangle fa-1-5x text-warning');
    }

    // create the task list in the UI
    $('#scc-task-list').addClass('fa-ul');
    for (i=0; i < tasks.length; ++i) {
      var message = $(tasks[i].messageKey).html();
      updateTaskMessageWithIcon(i + 1, message, 'fa-circle-o text-muted', 'text-muted');
    }

    // Run the list of tasks
    runTasks(tasks, cleanupTask, 1, tasks.length - 1);
  }

  // Bind the function to the dialog main div
  document.getElementById("scc-migration-dialog").refreshContent = refreshContent;
});

