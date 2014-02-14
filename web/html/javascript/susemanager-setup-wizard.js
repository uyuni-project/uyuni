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

// Hide any modal dialogs
function hideModal() {
  console.log("hideModal()");
  $('#edit-credentials-modal').modal('hide');
  $('#delete-credentials-modal').modal('hide');
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
function downloadSubscriptions(id) {
  $("#subscriptions-" + id).html("<i class='fa fa-spinner fa-spin'></i>");
  MirrorCredentialsRenderer.renderSubscriptions(id,
      makeAjaxCallback("subscriptions-" + id, false));
}

