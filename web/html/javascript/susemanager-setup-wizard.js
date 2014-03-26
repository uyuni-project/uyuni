
// Show spinner as inner HTML of an element given by ID
function showSpinner(elementId) {
  $("#" + elementId).html("<i class='fa fa-spinner fa-spin'></i>");
}

// relevant for the whole wizard
$(document).ready(function() {
  $.tabResizer();
});
