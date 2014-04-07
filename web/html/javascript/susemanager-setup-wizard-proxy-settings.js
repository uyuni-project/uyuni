// Save and verifiy the proxy settings
function saveProxySettings() {
  showSpinner('http-proxy-verify');
  $('#http-proxy-verify').show(100);
  $('#http-proxy-save').attr('disabled', true);

  hostname = $('#http-proxy-input-hostname').val();
  username = $('#http-proxy-input-username').val();
  password = $('#http-proxy-input-password').val();
  ProxySettingsRenderer.saveProxySettings({'hostname': hostname, 'username': username, 'password': password},
    function(settings) {
      console.log("Proxy settings saved!");
      $('#http-proxy-save').removeAttr('disabled');
      // TODO make sure it succeeded
      setProxySettings(settings);
      setProxySettingsEditable(false);

      // Force refresh of the cache
      verifyProxySettings(true);
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

// verify the proxy settings on the server side, pass true to refresh the cache
function verifyProxySettings(forceRefresh) {
  showSpinner('http-proxy-verify');
  ProxySettingsRenderer.verifyProxySettings(forceRefresh, function(valid) {
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
  ProxySettingsRenderer.retrieveProxySettings(
    function(settings) {
      setProxySettings(settings);
      console.log(JSON.stringify(settings));

      if (settings.hostname) {
        verifyProxySettings(false);
      } else {
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
    $('#http-proxy-save').show(100);
  } else {
    $('#http-proxy form input.form-control').hide();
    $('#http-proxy form p.form-control-static').show(100);
    $('#http-proxy-edit').show(100);
    $('#http-proxy-verify').show(100);
    $('#http-proxy-save').hide(100);
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
    verifyProxySettings(true);
  });

  setProxySettingsEditable(false);
  retrieveProxySettings();
});
