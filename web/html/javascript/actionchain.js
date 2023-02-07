jQuery(function() {
  var actionChainId = jQuery(".action-chain").data("action-chain-id");
  var maintenanceWindowsPresent = jQuery(".action-chain").data("maintenance-windows-present");

  // handle clik on title label
  jQuery("#label-link").on("click", function(){
    jQuery("#label-link").hide();
    jQuery("#label-input").show().focus();
    setUnsavedData();
    return false;
  });

  // handle clik on +/- icons
  jQuery(".system-list-show-hide").on("click", function() {
    var group = jQuery(this).closest(".group");
    var list = group.find(".system-list");
    var icon = group.find(".system-list-show-hide i");

    // change icon
    icon.toggleClass("fa-plus-square fa-minus-square");

    var listId = list.attr("id");
    var sortOrder = group.data("sort-order");
    // if needed, load list via Ajax
    if (list.is(":empty")) {
      ajax("action-chain-entries", { actionChainId, sortOrder }, makeRendererHandler(listId, false).callback)
    }
    else {
      list.fadeToggle();
    }
    return false;
  });

  // handle click on "delete action chain"
  jQuery("#delete-action-chain").on("click", function (event, target) {
    clearUnsavedData();
  });

  // handle click on "delete action" (that is: delete an action chain
  // entry group)
  jQuery(".delete-group").on("click", function (event, target) {
    var group = jQuery(this).closest(".group");

    group.fadeOut(400, renumberGroups).addClass("deleted");
    return false;
  });

  // handle click on "delete system" (that is: delete an action chain
  // entry)
  jQuery(".group").on("click", ".delete-entry", function (event, target) {
    li = jQuery(this).closest("li");
    ul = jQuery(this).closest("ul");
    var group = ul.closest(".group");

    if (ul.find("li:visible").length == 1) {
      group.fadeOut(400, renumberGroups).addClass("deleted");
    }
    else {
      li.fadeOut(400, function() {
        updateSystemCounter(ul, group);
      }).addClass("deleted");
    }
    return false;
  });

  // handle click on save changes
  jQuery("#save").on("click", function(){
    save(function onSuccess(text) {
      if (maintenanceWindowsPresent) {
        // only when we deal with maintenance windows
        // we will refresh the page (the datepicker must be reloaded)
        clearUnsavedData();
        location.reload();
      } else {
        jQuery("#error-message").hide();
        jQuery("#success-message").text(text).fadeIn();

        jQuery("#label-link-text").text(jQuery("#label-input").val());
        jQuery("#label-link").show();
        jQuery("#label-input").hide();
        clearUnsavedData();
      }
    });
    return false;
  });

  // handle click on cancel
  jQuery("#cancel").on("click", function() {
    clearUnsavedData();
    location.reload();
  });

  // handle click on save and schedule
  jQuery("#save-and-schedule").on("click", function() {
    save(function onSuccess(result) {
      clearUnsavedData();
      jQuery("form.schedule").submit();
    });
    return false;
  });

  // handle drag and drop
  jQuery(".action-chain").sortable({
    cursor: "move",
    update: renumberGroups
  });

  // handle exit without save
  jQuery(window).on("beforeunload", function() {
    if (jQuery.unsaved == true) {
      return jQuery("#before-unload").text();
    }
  });

  // save changes on Action Chain via AJAX
  function save(onSuccess) {
    var newLabel = jQuery("#label-input").val();
    var deletedEntries = jQuery(".entry.deleted").map(function(i, element) {
      return jQuery(element).data("entry-id");
    }).get();
    var deletedSortOrders = jQuery(".group.deleted").map(function(i, element) {
      return jQuery(element).data("sort-order");
    }).get();
    var reorderedSortOrders = jQuery(".group:not(.deleted)").map(function(i, element) {
      return jQuery(element).data("sort-order");
    }).get();

    const data = {
      actionChainId,
      label: newLabel,
      deletedEntries,
      deletedSortOrders,
      reorderedSortOrders
    }
    function handler(resultString) {
      const result = JSON.parse(resultString);
      if (result.success) {
        jQuery(".entry.deleted").remove();
        jQuery(".group.deleted").remove();
        jQuery(".group").each(function(i, element){
          jQuery(element).data("sort-order", i);
        });
        onSuccess(result.text);
      }
      else {
        jQuery("#success-message").hide();
        jQuery("#error-message").text(result.text).fadeIn();
      }
    }
    function onError(message) {
      console.log("Error processing ajax request... ", message)
      clearUnsavedData();
    }

    ajax("action-chain-save", data, handler, false, onError)
  }

  function renumberGroups(){
    jQuery(".group:visible").each(function(index, element) {
      jQuery(element).find(".counter").text(index + 1);
    });
    setUnsavedData();
  }

  function updateSystemCounter(ul, group) {
    var count = ul.find("li:visible").length;
    group.find(".system-counter").text(count);
    if (count == 1) {
      group.find(".singular-label").show();
      group.find(".plural-label").hide();
    }
    setUnsavedData();
  }

  function setUnsavedData() {
    jQuery.unsaved = true;
    jQuery("#action-chain-save-input").fadeIn();
  }

  function clearUnsavedData() {
    jQuery.unsaved = false;
    jQuery("#action-chain-save-input").fadeOut();
  }
});
