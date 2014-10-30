
$(function() {
  var button = $('#scc-start-migration-btn');
  var dialog = $('#scc-migration-dialog');
  var taskList = $('#scc-migration-dialog ul.dialog-steps');
  var dialogCloseBtn = $('#scc-migrate-dialog-close-btn');

  function addLastTask(message, iconClass) {
    var lastTask = taskList.children('li').last();
    var icon = $('<i></i>').attr('class', iconClass);
    var iconText = $('<div></div>');
    iconText.append(icon);
    iconText.append(message);
    var item = $('<li></li>').append(iconText);
    taskList.append(item);
  }

  function addTask(message) {
    addLastTask(message, 'fa fa-spinner fa-spin');
  }

  function taskFailed(message) {
    taskList.children().last('li').remove();
    addLastTask(message, 'fa fa-exclamation-triangle fa-1-5x text-warning');
  }

  function taskSucceeded(message) {
    taskList.children().last('li').remove();
    addLastTask(message, 'fa fa-check success');
  }

  // starts a task func with message. On completion
  // continues with task then
  function startTask(message, taskFunc, thenFunc, cleanupFunc) {
    addTask(message);
    taskFunc(makeAjaxHandler(
      function(ret) {
        taskSucceeded(message);
        //alert(message);
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
    dialogCloseBtn.prop('disabled', true);

    function cleanupFunc() {
        dialogCloseBtn.prop('disabled', false);
    }

    // as one tasks succeeeds, start the next
    startTask($('#sccconfig\\.jsp\\.channels').html(), SCCConfigAjax.sayHello, function() {
        startTask($('#sccconfig\\.jsp\\.channelfamilies').html(), SCCConfigAjax.sayHello, function() {
          startTask($('#sccconfig\\.jsp\\.products').html(), SCCConfigAjax.sayHello, function() {
            startTask($('#sccconfig\\.jsp\\.productchannels').html(), SCCConfigAjax.sayHello, function() {
              startTask($('#sccconfig\\.jsp\\.subscriptions').html(), SCCConfigAjax.sayHello, function() {
              startTask($('#sccconfig\\.jsp\\.upgradepaths').html(), SCCConfigAjax.sayHello, function() {
                button.hide();
                button.prop('disabled', false);
                dialogCloseBtn.prop('disabled', false);
              }, cleanupFunc);
            }, cleanupFunc);
          }, cleanupFunc);
        }, cleanupFunc);
      }, cleanupFunc);
    }, cleanupFunc);
  });
});
