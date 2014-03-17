// Temporarily store credential IDs
// XXX: One variable should be enough?
var editId;
var deleteId;
var subscriptionsId;

// Init modal to edit credentials
function initEdit(id, email, user) {
  console.log("initEdit(): " + id);
  editId = id;
  $('#edit-email').val(email);
  $('#edit-user').val(user);
  $('#edit-password').val("");
}

// Init modal to delete credentials
function initDelete(id, email, user) {
  console.log("initDelete(): " + id);
  deleteId = id;
  $('#delete-email').text(email);
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

// Show spinner as inner HTML of an element given by ID
function showSpinner(elementId) {
  $("#" + elementId).html("<i class='fa fa-spinner fa-spin'></i>");
}

// Save credentials from edit dialog
function saveCredentials() {
  console.log("Saving credentials: " + editId);
  var email = $('#edit-email').val();
  var user = $('#edit-user').val();
  var password = $('#edit-password').val();
  showSpinner("edit-credentials-spinner");
  MirrorCredentialsRenderer.saveCredentials(editId, email, user, password,
      makeAjaxCallback("listset-container", false));
}

// Delete credentials from modal
function deleteCredentials() {
  showSpinner("delete-credentials-spinner");
  MirrorCredentialsRenderer.deleteCredentials(deleteId,
      makeAjaxCallback("listset-container", false));
}

// Make primary credentials
function makePrimaryCredentials(id) {
  showSpinner("primary-" + id);
  MirrorCredentialsRenderer.makePrimaryCredentials(id,
      makeAjaxCallback("listset-container", false));
}

// Verify credentials by downloading subscriptions
function verifyCredentials(id, refresh) {
  showSpinner("verify-" + id);
  MirrorCredentialsRenderer.verifyCredentials(id, refresh,
      makeAjaxCallback("verify-" + id, false));
}

// Stuff to do when document is ready
$(document).ready(function() {
  // Clear input fields whenever the edit modal is hidden
  $('#modal-edit-credentials').on('hidden.bs.modal', function() {
    initEdit("", "", "");
  });

  // Load subscriptions when modal is shown
  $('#modal-list-subscriptions').on('show.bs.modal', function() {
    showSpinner("modal-list-subscriptions-body");
    MirrorCredentialsRenderer.listSubscriptions(subscriptionsId,
      makeAjaxCallback("modal-list-subscriptions-body", false));
  });
});

