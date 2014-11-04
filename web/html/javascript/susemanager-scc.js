$(function() {
  $("#scc-start-migration-btn").on("click", function() {
    var button = $('#scc-start-migration-btn');
    button.prop('disabled', true);
    refreshContent($("#sccconfig\\.jsp\\.switchingtoscc").html(), true, true, function() {
      button.hide();
      button.prop('disabled', false);
    });
  });
});

