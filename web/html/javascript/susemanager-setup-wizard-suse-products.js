$(function() {
  showProducts();

  // handles select all top/bottom checkboxes
  $("input.select-all").change(function() {
    $("#suse-products input[type='checkbox']:enabled").prop("checked", $(this).is(":checked"));
  });

  // handles expand/collapse buttons and text
  $(".table-content").on("click", ".collapsible", function() {
    var row = $(this).closest("tr");
    toggleExpansion(false, row);
  });

  // handles clicks on product checkboxes
  $(".table-content").on("change", "input", function(event) {
    var checked = $(this).prop("checked");
    var disabled = $(this).prop("disabled");
    var row = $(this).closest("tr");
    var productIdent = row.data("ident");
    var baseProductIdent = row.data("baseproductident");
    var base = baseProductIdent === "";

    if (base) {
      // Enable/disable sync buttons for addon products
      toggleAddonSyncButtons(productIdent, !(checked && disabled));
    }
    if (!checked && base) {
      $("tr[data-baseproductident='" + productIdent + "']")
        .find("input")
        .prop("checked", false).trigger("change");
    }
    if (checked && !base) {
      $("tr[data-ident='" + baseProductIdent + "']")
        .find("input")
        .prop("checked", true).trigger("change");
    }
    if (checked && base) {
      toggleExpansion(true, row);
    }

    // highlight selected and provided productsn() {
    var cssClass = disabled ? "product-provided" : "product-selected";
    if (checked) {
      $(this).closest("tr").addClass(cssClass);
    }
    else {
      $(this).closest("tr").removeClass(cssClass);
    }
  });

  // Handle the bottom button
  $("#synchronize").click(function() {
    var checkboxes = $(".table-content input[type='checkbox']:checked:enabled");
    triggerProductSync(checkboxes, $(this));
  });

  // Handle add product buttons
  $(".table-content").on("click", ".product-add-btn", function() {
    var checkbox = $(this).closest('tr').find('input:checkbox');
    triggerProductSync(checkbox, $(this));
  });

  // Handle retry button (hidden by default, except when activated from modal)
  $(".table-content").on("click", ".product-retry-btn", function() {
    var checkbox = $(this).closest('tr').find('input:checkbox');
    triggerProductSync(checkbox, $(this));
  });

  // Trigger product addition or sync server side for a given array of checkboxes
  function triggerProductSync(checkboxes, buttonClicked) {
    var idents = checkboxes.closest("tr").map(function() {
      return $(this).data("ident");
    }).toArray();

    // Do nothing if no products are selected
    if (idents.length === 0) {
      return;
    }

    // Show spinner for the clicked button
    var icon = buttonClicked.find('i');
    icon.removeClass("fa-plus");
    icon.addClass("fa-spinner");
    icon.addClass("fa-spin");

    // Disable all sync buttons until we are back
    $('#synchronize').prop('disabled', true);
    $('button.product-add-btn').prop('disabled', true);
    $('button.product-retry-btn').prop('disabled', true);

    // Trigger product sync server side
    ProductSyncAction.syncProducts(idents, makeAjaxHandler(function() {
      $.each(checkboxes, function() {
        $(this).prop("checked", true);
        $(this).prop("disabled", true).trigger("change");

        $.each($(this).closest("tr").find("div.product-status, div.product-status-action"), function () {
          var status = $(this).data("syncstatus");
          if (status === "in_progress") {
            $(this).show();
          } else {
            $(this).hide();
          }
        });
      });

      // Reset spinner to download icon
      icon.removeClass("fa-spin");
      icon.removeClass("fa-spinner");
      icon.addClass("fa-plus");

      // Re-enable and re-init sync buttons
      $('#synchronize').prop('disabled', false);
      $('button.product-add-btn').prop('disabled', false);
      $('button.product-retry-btn').prop('disabled', false);
      initSyncButtons();
    }));
  }

  // Get the products or show message in case of errors
  function showProducts() {
    ProductsRenderer.renderAsync(makeAjaxHandler(
      function(content) {
        $("#loading-placeholder").hide();
        $(".table-content").append(content);
        $('.product-add-btn').tooltip();
        $('.product-retry-btn').tooltip();
        $(".product-channels-btn").tooltip();
        $(".product-channels-btn").click(function() {
            $(this).closest('tr').find('.product-channels-modal').modal('show')
        });

        $(".product-status-label .product-sync-log-btn").click(function() {
            $(this).closest('tr').find('.product-sync-log-modal').modal('show');
        });
        initSyncButtons();
        humanizeDates();
      },
      function(message, exception) {
        $('.table').hide();
        if (exception.javaClassName.indexOf("InvalidMirrorCredentialException") > 0) {
          $("#invalid-credentials-alert-popup").show();
        } else if (exception.javaClassName.indexOf("ConnectionException") > 0) {
          $("#no-connection-alert-popup").show();
        } else {
          showFatalError();
        }
      })
    );
  }

  // Initially enable/disable sync buttons for addon products
  function initSyncButtons() {
    $("input[type='checkbox'].select-single").each(function() {
      var row = $(this).closest("tr");
      var baseProductIdent = row.data("baseproductident");
      var base = baseProductIdent === "";

      if (base) {
        var productIdent = row.data("ident");
        var checked = $(this).prop("checked");
        var disabled = $(this).prop("disabled");
        toggleAddonSyncButtons(productIdent, !(checked && disabled));
      }
    });
  }

  // Toggle sync buttons for addons of a given base product
  function toggleAddonSyncButtons(baseProductIdent, disabled) {
    $("tr[data-baseproductident='" + baseProductIdent + "']")
      .find("button.product-add-btn")
      .prop("disabled", disabled);
  }

  // expands or collapses a row, expects $(this) to point to an element
  // that is a descendant of a row.
  // Use forceExpand to force expansion (never collapse)
  function toggleExpansion(forceExpand, row) {
    var expandButton = row.find(".expand-button");
    var collapseButton = row.find(".collapse-button");
    var addonRows = $("tr[data-baseproductident='" + row.data("ident") + "']");

    if (forceExpand) {
      expandButton.hide();
      collapseButton.show();
      addonRows.show();
    } else {
      expandButton.toggle();
      collapseButton.toggle();
      addonRows.toggle();
    }
  }
});
