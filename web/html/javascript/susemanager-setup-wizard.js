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

// Save and verifiy the proxy settings
function saveProxySettings() {
  showSpinner('http-proxy-verify');
  $('#http-proxy-verify').show(100);
  $('#http-proxy-save').attr('disabled', true);

  hostname = $('#http-proxy-input-hostname').val();
  username = $('#http-proxy-input-username').val();
  password = $('#http-proxy-input-password').val();
  HttpProxyRenderer.saveProxySettings({'hostname': hostname, 'username': username, 'password': password},
    function(settings) {
      console.log("Proxy settings saved!");
      $('#http-proxy-save').removeAttr('disabled');
      // TODO make sure it succeeded
      setProxySettings(settings);
      setProxySettingsEditable(false);
      verifyProxySettings();
    }
  );
}

// sets in the UI if the proxy settings were verified
function setProxySettingsVerified(valid) {
  if (valid) {
    $('#http-proxy-verify').html('<i class="fa fa-check-square text-success"></i>');
  } else {
    $('#http-proxy-verify').html('<i class="fa fa-times-circle-o text-danger"></i>');
  }
}

// verify the progress settings on the server side
function verifyProxySettings(settings) {
  showSpinner('http-proxy-verify');
  HttpProxyRenderer.verifyProxySettings(function(valid) {
    console.log("verified proxy: " + valid);
    setProxySettingsVerified(valid);
  });
}

// just sets the given settings in the form
function setProxySettings(settings) {
  $('#http-proxy-input-hostname').val(settings.hostname);
  $('p.http-proxy-hostname').html(settings.hostname);

  $('#http-proxy-input-username').val(settings.username);
  $('p.http-proxy-username').html(settings.username);

  $('#http-proxy-input-password').val(settings.password);
  $('p.http-proxy-password').html(Array(settings.password.length).join('&#9679'));
}

// Sets the spinner, retrieves the settings from the server
// and puts them in the UI, which ends in an editable form
// if there is no proxy set
function retrieveProxySettings() {
  showSpinner('http-proxy-verify');
  HttpProxyRenderer.retrieveProxySettings(
    function(settings) {
      setProxySettings(settings);
      console.log(JSON.stringify(settings));

      if (settings.hostname) {
        verifyProxySettings(settings);
      }
      else {
        setProxySettingsEditable(true);
      }
    }
  );
}

// Switches the proxy settings into an (non)editable form
function setProxySettingsEditable(editable) {
  if (editable) {
    $('#http-proxy form p.form-control-static').hide();
    $('#http-proxy form input.form-control').show(100);
    $('#http-proxy-edit').hide(100);
    $('#http-proxy-verify').hide(100);
  } else {
    $('#http-proxy form input.form-control').hide();
    $('#http-proxy form p.form-control-static').show(100);
    $('#http-proxy-edit').show(100);
    $('#http-proxy-verify').show(100);
  }
}

// only relevant for the proxy settings
$(document).ready(function() {

  // set the edit button callback
  $('#http-proxy-edit').click(function() {
    setProxySettingsEditable(true);
  });

  // set the save button callback
  $('#http-proxy-save').click(function() {
    saveProxySettings();
  });

  $('#http-proxy-verify').click(function() {
    verifyProxySettings();
  });

  setProxySettingsEditable(false);
  retrieveProxySettings();
});

// relevant for the mirror credentials page
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

// relevant for the whole wizard
$(document).ready(function() {
  $.tabResizer();
});
