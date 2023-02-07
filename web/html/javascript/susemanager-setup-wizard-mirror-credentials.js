
// Temporarily store credential IDs
// XXX: One variable should be enough?
var editId;
var deleteId;
var subscriptionsId;

// Init modal to edit credentials
function initEdit(id, user) {
  console.log("initEdit(): " + id);
  editId = id;
  jQuery('#edit-user').val(user);
  jQuery('#edit-password').val("");
  jQuery('#mirror-credentials-error-container').hide(); // Hide the error container
  jQuery('.mirror-credentials-error').hide(); // Make sure all error elements are hidden
}

// Init modal to delete credentials
function initDelete(id, user) {
  console.log("initDelete(): " + id);
  deleteId = id;
  jQuery('#delete-user').text(user);
}

// Init the modal to list subscriptions
function initSubscriptions(id) {
  console.log("initSubscriptions(): " + id);
  subscriptionsId = id;
}

// Hide any modal dialogs
function hideModal() {
  console.log("hideModal()");
  jQuery("#edit-credentials-spinner").html("");
  jQuery("#modal-edit-credentials").modal('hide');
  jQuery("#delete-credentials-spinner").html("");
  jQuery("#modal-delete-credentials").modal('hide');
}

// Save credentials from edit dialog
function saveCredentials() {
  var validated = true;
  jQuery('#add-credentials-form .form-control[required]').each(function() {
    var field = jQuery(this);
    if (field.val().length == 0) {
      field.closest('.form-group').addClass('has-error');
      validated = false;
    }
    else {
      field.closest('.form-group').removeClass('has-error');
    }
  });

  if (validated) {
    console.log("Saving credentials: " + editId);
    var user = jQuery('#edit-user').val();
    var password = jQuery('#edit-password').val();
    showSpinner("edit-credentials-spinner");

    var responseHandler = function(result) {
      if (result == "ok") {
        ajax("render-mirror-credentials", "", makeRendererHandler("listset-container", false).callback)
      }
      else {
        jQuery("#mirror-credentials-error-container").show(); // show the error container
        jQuery("#" + result).show(); //result contains the id of the error element to be shown
      }
    };

    ajax("save-mirror-credentials", { id: editId, user, password }, responseHandler)

    jQuery("#edit-credentials-spinner").hide();
  }
}

// Delete credentials from modal
function deleteCredentials() {
  showSpinner("delete-credentials-spinner");
  ajax('delete-mirror-credentials', { id: deleteId }, makeRendererHandler("listset-container", false).callback)
}

// Make primary credentials
function makePrimaryCredentials(id) {
  showSpinner("primary-" + id);
  ajax("make-primary-mirror-credentials", { id }, makeRendererHandler("listset-container", false).callback)
}

function setDeleteAllowed(id, allowed) {
  const linkElem = jQuery('#delete-' + id + ' a:first-child');
  const iconElem = linkElem.children('i:first-child');

  let linkStyle = {'pointer-events': 'all'};
  let iconStyle = {'cursor': 'pointer', 'color': ''};

  if (!allowed) {
    linkStyle = {'pointer-events': 'none'};
    iconStyle = {'cursor': 'not-allowed', 'color': 'gray'};
  }

  linkElem.css(linkStyle);
  iconElem.css(iconStyle);
}

// Verify credentials by downloading subscriptions
function verifyCredentials(id, refresh) {
  const elemId = "verify-" + id;
  const responseHandler = (result) => {
    jQuery('#' + elemId).html(result);
    jQuery('#' + elemId).fadeIn();
    columnHeight();
    setDeleteAllowed(id, true);
  };

  showSpinner(elemId);
  setDeleteAllowed(id, false);
  ajax("verify-mirror-credentials", { id, refresh }, responseHandler, "application/json")
}

// relevant for the mirror credentials page
jQuery(document).ready(function() {
  // Clear input fields whenever the edit modal is hidden
  jQuery('#modal-edit-credentials').on('hidden.bs.modal', function() {
    initEdit("", "");
  });

  // Load subscriptions when modal is shown
  jQuery('#modal-list-subscriptions').on('show.bs.modal', function() {
    showSpinner("modal-list-subscriptions-body");
    ajax("list-mirror-subscriptions", { subscriptionsId }, makeRendererHandler("modal-list-subscriptions-body", false).callback)
  });
});
