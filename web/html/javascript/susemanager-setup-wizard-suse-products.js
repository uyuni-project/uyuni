$(function(){
  refreshProducts();

  $("#synchronize").click(refreshProducts);

  // click on an add product icon. Handler is attached to table because rows
  // can be reloaded via AJAX
  $("#table-content").on("click", ".start-sync-icon", function(event) {
    var startSyncIcon = $(this);
    var ident = startSyncIcon.data("ident");

    var startingSyncIcon = startSyncIcon.siblings(".starting-sync-icon");
    var syncStartedIcon = startSyncIcon.siblings(".sync-started-icon");
    var addonStartSyncIcons = $(".start-sync-icon[data-baseproductident='" + ident + "']");

    startSyncIcon.hide();
    startingSyncIcon.show();

    ProductSyncAction.synchronizeSingle(ident, function() {
      startingSyncIcon.hide();
      syncStartedIcon.show();
      addonStartSyncIcons.show();
    });
  });

  function refreshProducts() {
    $("#loading-placeholder").show();
    $("#table-content tr:not(#loading-placeholder)").remove();
    ProductsRenderer.renderAsync(function(content) {
      $("#loading-placeholder").hide();
      $("#table-content").append(content);
    });
  }
});
