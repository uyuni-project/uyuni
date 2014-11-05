$(function() {
  // Reload the page after the dialog is closed
  $('#scc-migration-dialog').on('hidden.bs.modal', function() {
    location.reload();
  });

  $("#scc-start-migration-btn").on("click", function() {
    var button = $('#scc-start-migration-btn');
    button.prop('disabled', true);
    var refresh = $("#iss-master").data("iss-master");
    document.getElementById("scc-migration-dialog").refreshContent(
        $("#sccconfig\\.jsp\\.switchingtoscc").html(), true, refresh, function() {
      button.hide();
      button.prop('disabled', false);
    });
  });
});

