$(document).on("ready", function() {
  // Setup SSE event source to receive events
  function setupEventSource() {
    if (typeof(EventSource) !== "undefined") {
      var source = new EventSource("/rhn/sse");
      source.addEventListener("message", function(event) {
        console.log("Received event: " + event.data);

        // Create an alert and add it to spacewalk-content
        var alert = $('<div class="alert alert-danger"/>');
        var data = JSON.parse(event.data);
        var modalId = data.minionId + Date.now();
        alert.html('Managed file <b>' + data.path +
            '</b> has been modified on minion: <a href="/rhn/manager/minions/overview/' + data.minionId +'"><b>' + data.minionId + '</b></a>' +
            '<button data-toggle="modal" data-target="#' + modalId + '" class="btn btn-danger pull-right">See Changes</button>' +
            '<span class="clearfix"></span>' +
            '<div class="modal fade" id="' + modalId + '" tabindex="-1" role="dialog" aria-labelledby="diff">' +
              '<div class="modal-dialog">' +
                '<div class="modal-content">' +
                  '<div class="modal-header">' +
                    '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                    '<h4 class="modal-title" id="myModalLabel">Changes in <code>' + data.path + '</code></h4>' +
                  '</div>' +
                  '<div class="modal-body">' +
                    '<pre style="overflow: auto; max-height: 500px;">' + data.diff + '</pre>' +
                  '</div>' +
                  '<div class="modal-footer">' +
                    '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                  '</div>' +
                '</div>' +
              '</div>' +
            '</div>'
            );
        $("#spacewalk-content").prepend(alert);
      }, false);
      source.onerror = function(error) {
        console.log("Event stream error");
      };
      console.log("Event listener added");
    } else {
      console.log("Sorry, Server-Sent Events are not supported in your browser");
    }
    return false;
  }

  // Connect to the event stream after staying on the page for a while
  window.setTimeout(setupEventSource, 4000);
});
