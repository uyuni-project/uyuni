$(function(){
  refreshProducts();

  $("#synchronize").click(refreshProducts);

  // click on an add product icon. Handler is attached to table because rows
  // can be reloaded via AJAX
  $("#table-content").on("click", ".start-sync-icon", function(event) {
    var startSyncIcon = $(this);
    var ident = startSyncIcon.closest("tr").data("ident");

    var startingSyncIcon = startSyncIcon.siblings(".starting-sync-icon");
    var syncStartedIcon = startSyncIcon.siblings(".sync-started-icon");
    var addonRows = $("tr[data-baseproductident='" + ident + "']");
    var addonStartSyncIcons =  addonRows.find(".start-sync-icon");
    var checkbox = $("tr[data-ident='" + ident + "'] input[type='checkbox']");
    var addonCheckboxes = addonRows.find("input[type='checkbox']");

    startSyncIcon.hide();
    startingSyncIcon.show();

    ProductSyncAction.synchronizeSingle(ident, function() {
      startingSyncIcon.hide();
      syncStartedIcon.show();
      addonStartSyncIcons.show();

      checkbox.prop("checked", false).prop("disabled", true);
      addonCheckboxes.removeAttr("disabled");
    });
  });

  // handles select all top/bottom checkboxes
  $("input.select-all").change(function() {
    $("#suse-products input[type='checkbox']:enabled").prop("checked", $(this).is(":checked"));
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
