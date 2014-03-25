
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
