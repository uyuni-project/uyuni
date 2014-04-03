$(function(){
  refreshProducts();

  $("#synchronize").click(refreshProducts);

  // click on an add product icon. Handler is attached to table because rows
  // can be reloaded via AJAX
  $("#table-content").on("click", ".synchronize-single", function(event) {
    ProductSyncAction.synchronizeSingle($(event.target).data("ident"), function() {
      refreshProducts();
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
