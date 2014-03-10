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
  $('#modal-edit-credentials').modal('hide');
  $('#modal-delete-credentials').modal('hide');
}

// Save credentials from edit dialog
function saveCredentials() {
  var email = $('#edit-email').val();
  var user = $('#edit-user').val();
  var password = $('#edit-password').val();
  console.log("Saving credentials: " + editId);
  MirrorCredentialsRenderer.saveCredentials(editId, email, user, password,
      makeAjaxCallback("listset-container", false));
}

// Delete credentials from modal
function deleteCredentials() {
  $("#delete-" + deleteId).html("<i class='fa fa-spinner fa-spin'></i>");
  MirrorCredentialsRenderer.deleteCredentials(deleteId,
      makeAjaxCallback("listset-container", false));
}

// Verify credentials by downloading subscriptions
function verifyCredentials(id) {
  $("#verify-" + id).html("<i class='fa fa-spinner fa-spin'></i>");
  MirrorCredentialsRenderer.verifyCredentials(id,
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
    $("#modal-list-subscriptions-body").html("<i class='fa fa-spinner fa-spin'></i>");
    MirrorCredentialsRenderer.listSubscriptions(subscriptionsId,
      makeAjaxCallback("modal-list-subscriptions-body", false));
  });
});
