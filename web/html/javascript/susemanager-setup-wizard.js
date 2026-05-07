// Show spinner as inner HTML of an element given by ID
function showSpinner(elementId) {
  jQuery("#" + elementId).html("<div class='wizard-spin'><i class='fa fa-spinner fa-spin'></i></div>");
}
