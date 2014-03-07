// Init the edit modal with given values
function initEdit(id, email, user) {
  console.log("initEdit(): " + id);
  $('#modal-id').val(id);
  $('#modal-email').val(email);
  $('#modal-user').val(user);
  $('#modal-password').val("");
}

// Init the delete confirmation modal with given values
function initDelete(id, email, user) {
  console.log("initDelete(): " + id);
  $('#delete-id').val(id);
  $('#delete-email').text(email);
  $('#delete-user').text(user);
}

// Temp store for credentials ID to list subscriptions for
var subscriptionsId;

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

// Save credentials from modal dialog
function saveCredentials() {
  var id = $('#modal-id').val();
  var email = $('#modal-email').val();
  var user = $('#modal-user').val();
  var password = $('#modal-password').val();
  initEdit("", "", "");
  console.log("Saving credentials: " + id);
  MirrorCredentialsRenderer.saveCredentials(id, email, user, password,
      makeAjaxCallback("listset-container", false));
}

// Delete credentials from confirmation modal
function deleteCredentials() {
  var id = $('#delete-id').val();
  $("#delete-" + id).html("<i class='fa fa-spinner fa-spin'></i>");
  MirrorCredentialsRenderer.deleteCredentials(id,
      makeAjaxCallback("listset-container", false));
}

// Download the subscriptions for given ID
function verifyCredentials(id) {
  $("#verify-" + id).html("<i class='fa fa-spinner fa-spin'></i>");
  MirrorCredentialsRenderer.verifyCredentials(id,
      makeAjaxCallback("verify-" + id, false));
}

// Stuff to do when document is ready
$(document).ready(function() {
  // Register listener to load subscriptions when modal is shown
  $('#modal-list-subscriptions').on('show.bs.modal', function() {
    $("#modal-list-subscriptions-body").html("<i class='fa fa-spinner fa-spin'></i>");
    MirrorCredentialsRenderer.listSubscriptions(subscriptionsId,
      makeAjaxCallback("modal-list-subscriptions-body", false));
  })
});

