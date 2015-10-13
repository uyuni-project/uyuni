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
        alert.html('Managed file <b>' + data.path +
            '</b> has been modified on minion: <b>' + data.minionId + '</b>');
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
