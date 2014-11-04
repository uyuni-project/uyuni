$(function() {
  $("#scc-start-migration-btn").on("click", function() {
    var button = $('#scc-start-migration-btn');
    button.prop('disabled', true);
    var refresh = $("#iss-master").data("iss-master");
    refreshContent($("#sccconfig\\.jsp\\.switchingtoscc").html(), true, refresh, function() {
      button.hide();
      button.prop('disabled', false);
    });
  });
});

