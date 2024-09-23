// Save and verifiy the proxy settings
function saveProxySettings() {
  showSpinner('http-proxy-verify');
  jQuery('#http-proxy-verify').show(0);
  jQuery('#http-proxy-save').attr('disabled', true);

  hostname = jQuery('#http-proxy-input-hostname').val();
  username = jQuery('#http-proxy-input-username').val();
  password = jQuery('#http-proxy-input-password').val();
  
  function onSuccess(settings) {
    jQuery('#http-proxy-save').prop('disabled', false);
    // TODO make sure it succeeded
    setProxySettings(settings);
    setProxySettingsEditable(false);

    // Force refresh of the cache
    verifyProxySettings(true);
  }

  ajax("save-proxy-settings", { hostname, username, password }, onSuccess)
}

// sets in the UI if the proxy settings were verified
function setProxySettingsVerified(result) {
  const valid = JSON.parse(result)
  if (valid) {
    jQuery('#http-proxy-verify').html('<i class="fa fa-check-square text-success"></i>');
  } else {
    jQuery('#http-proxy-verify').html('<i class="fa fa-times-circle-o text-danger"></i>');
  }
}

// verify the proxy settings on the server side, pass true to refresh the cache
function verifyProxySettings(forceRefresh) {
  showSpinner('http-proxy-verify');
  ajax('verify-proxy-settings', { forceRefresh }, setProxySettingsVerified, 'application/json')
}

// just sets the given settings in the form
function setProxySettings(settings) {
  jQuery('#http-proxy-input-hostname').val(settings.hostname);
  jQuery('p.http-proxy-hostname').html(settings.hostname);

  jQuery('#http-proxy-input-username').val(settings.username);
  jQuery('p.http-proxy-username').html(settings.username);

  if (settings.hostname) {
    jQuery('p.http-proxy-password').html(Array(8).join('&#9679'));
  } else {
    jQuery('p.http-proxy-password').html("");
  }
}

// Sets the spinner, retrieves the settings from the server
// and puts them in the UI, which ends in an editable form
// if there is no proxy set
function retrieveProxySettings() {
  showSpinner('http-proxy-verify');
  
  function onSuccess(settings) {
    setProxySettings(settings);

    if (settings.hostname) {
      verifyProxySettings(false);
    } else {
      setProxySettingsEditable(true);
    }
  }

  ajax('retrieve-proxy-settings', '', onSuccess, 'application/json')
}

// Switches the proxy settings into an (non)editable form
function setProxySettingsEditable(editable) {
  if (editable) {
    jQuery('#http-proxy form p.form-control-static').hide();
    jQuery('#http-proxy form input.form-control').show(0);
    jQuery('#http-proxy-edit').hide(0);
    jQuery('#http-proxy-verify').hide(0);
    jQuery('#http-proxy-save').show(0);
  } else {
    jQuery('#http-proxy form input.form-control').hide();
    jQuery('#http-proxy form p.form-control-static').show(0);
    jQuery('#http-proxy-edit').show(0);
    jQuery('#http-proxy-verify').show(0);
    jQuery('#http-proxy-save').hide(0);
  }
}

// only relevant for the proxy settings
jQuery(document).ready(function() {
  // set the edit button callback
  jQuery('#http-proxy-edit').on("click", function() {
    setProxySettingsEditable(true);
  });

  // set the save button callback
  jQuery('#http-proxy-save').on("click", function() {
    saveProxySettings();
  });

  jQuery('#http-proxy-verify').on("click", function() {
    verifyProxySettings(true);
  });

  setProxySettingsEditable(false);
  retrieveProxySettings();
});
