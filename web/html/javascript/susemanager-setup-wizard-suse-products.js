$(function(){
  showProducts();

  // handles select all top/bottom checkboxes
  $("input.select-all").change(function() {
    $("#suse-products input[type='checkbox']:enabled").prop("checked", $(this).is(":checked"));
  });

  // handles expand/collapse buttons
  $("#table-content").on("click", ".expand-button", function(event) {
    $(this).hide();
    $(this).siblings(".collapse-button").show();
    $("tr[data-baseproductident='" + $(this).closest("tr").data("ident") + "']").show();
    return false;
  });

  $("#table-content").on("click", ".collapse-button", function(event) {
    $(this).hide();
    $(this).siblings(".expand-button").show();
    $("tr[data-baseproductident='" + $(this).closest("tr").data("ident") + "']").hide();
    return false;
  });

  // handles synchronize bottom button
  $("#synchronize").click(function() {
    var checkboxes = $("#table-content input[type='checkbox']:checked:enabled");
    var idents = checkboxes.closest("tr").map(function() {
      return $(this).data("ident");
    }).toArray();

    var button = $(this);
    var icon = button.find("i");            
    button.prop("disabled", true);
    icon.addClass("fa-spin");

    ProductSyncAction.synchronize(idents, makeAjaxHandler(function() {
      $.each(checkboxes, function() {
        $(this).prop("checked", true);
        $(this).prop("disabled", true);
        
        $.each($(this).closest("tr").find("span"), function () {
          var status = $(this).data("syncstatus");
          if (status === "in_progress") {
            $(this).show();
          } else {
            $(this).hide();
          }
        });
      });

      icon.removeClass("fa-spin");      
      button.prop("disabled", false);
    }));
  });

  // handles clicks on product checkboxes
  $("#table-content").on("change", "input", function(event) {
    var checked = $(this).prop("checked");
    var row = $(this).closest("tr");
    var baseProductIdent = row.data("baseproductident");
    var base = baseProductIdent === "";

    if (!checked && base) {
      var productIdent = row.data("ident");
      $("tr[data-baseproductident='" + productIdent + "']")
        .find("input")
        .prop("checked", false);
    }
    if (checked && !base) {
      $("tr[data-ident='" + baseProductIdent + "']")
        .find("input")
        .prop("checked", true);
    }
  });

  // Get the products and show message in case of errors
  function showProducts() {
    ProductsRenderer.renderAsync(makeAjaxHandler(
      function(content) {
        $("#loading-placeholder").hide();
        $("#table-content").append(content);
      },
      function(message, exception) {
        if (exception.javaClassName.indexOf("InvalidMirrorCredentialException") > 0) {
          $('.table').hide();
          $("#alert-popup").show();
        }
        else {
          showFatalError();
        }
      })
    );
  }
});
