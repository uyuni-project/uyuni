
// Temporarily store credential IDs
// XXX: One variable should be enough?
var editId;
var deleteId;
var subscriptionsId;

// Init modal to edit credentials
function initEdit(id, user) {
  console.log("initEdit(): " + id);
  editId = id;
  $('#edit-user').val(user);
  $('#edit-password').val("");
  $('#mirror-credentials-error-container').hide(); // Hide the error container
  $('.mirror-credentials-error').hide(); // Make sure all error elements are hidden
}

// Init modal to delete credentials
function initDelete(id, user) {
  console.log("initDelete(): " + id);
  deleteId = id;
  $('#delete-user').text(user);
}

// Init the modal to list subscriptions
function initSubscriptions(id) {
  console.log("initSubscriptions(): " + id);
  subscriptionsId = id;
}

// Hide any modal dialogs
function hideModal() {
  console.log("hideModal()");
  $("#edit-credentials-spinner").html("");
  $("#modal-edit-credentials").modal('hide');
  $("#delete-credentials-spinner").html("");
  $("#modal-delete-credentials").modal('hide');
}

// Save credentials from edit dialog
function saveCredentials() {
  var validated = true;
  $('#add-credentials-form .form-control[required]').each(function() {
    var field = $(this);
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
    var user = $('#edit-user').val();
    var password = $('#edit-password').val();
    showSpinner("edit-credentials-spinner");

    var responseHandler = function(result) {
      if (result == "ok") {
        MirrorCredentialsRenderer.renderCredentials(makeRendererHandler("listset-container", false));
      }
      else {
        $('#mirror-credentials-error-container').show(); // show the error container
        $('#' + result).show(); //result contains the id of the error element to be shown
      }
    };

    MirrorCredentialsRenderer.saveCredentials(editId, user, password,
        makeAjaxHandler(responseHandler));

    $("#edit-credentials-spinner").hide();
  }
}

// Delete credentials from modal
function deleteCredentials() {
  showSpinner("delete-credentials-spinner");
  MirrorCredentialsRenderer.deleteCredentials(deleteId,
      makeRendererHandler("listset-container", false));
}

// Make primary credentials
function makePrimaryCredentials(id) {
  showSpinner("primary-" + id);
  MirrorCredentialsRenderer.makePrimaryCredentials(id,
      makeRendererHandler("listset-container", false));
}

function setDeleteAllowed(id, allowed) {
  const linkElem = $('#delete-' + id + ' a:first-child');
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
    $('#' + elemId).html(result);
    $('#' + elemId).fadeIn();
    columnHeight();
    setDeleteAllowed(id, true);
  };

  showSpinner(elemId);
  setDeleteAllowed(id, false);
  MirrorCredentialsRenderer.verifyCredentials(id, refresh,
      makeAjaxHandler(responseHandler));
}

// relevant for the mirror credentials page
$(document).ready(function() {
  // Clear input fields whenever the edit modal is hidden
  $('#modal-edit-credentials').on('hidden.bs.modal', function() {
    initEdit("", "");
  });

  // Load subscriptions when modal is shown
  $('#modal-list-subscriptions').on('show.bs.modal', function() {
    showSpinner("modal-list-subscriptions-body");
    MirrorCredentialsRenderer.listSubscriptions(subscriptionsId,
      makeRendererHandler("modal-list-subscriptions-body", false));
  });
});
