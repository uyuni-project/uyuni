jQuery(function() {
  // Show child channels of selected base channel
  function showChannelTree(channelID) {
    jQuery('.channels-tree#channels-tree-' + channelID).show();
    jQuery('.channels-tree[id != channels-tree-' + channelID + ']').hide();
  }

  // Create hidden inputs to submit channel IDs
  function prepareSubmitChannels() {
    jQuery('#migrationForm input[type="hidden"][name="childChannels"]').remove();
    // Submit all checked child channel's IDs
    jQuery('.channels-tree:visible input:checked').each(function() {
      jQuery('#migrationForm').append(
        createHiddenInput('childChannels', jQuery(this).val())
      );
    });
    return true;
  }

  // Create a hidden input
  function createHiddenInput(name, value) {
    var el = document.createElement("input");
    el.type = "hidden";
    el.name = name;
    el.value = value;
    return el;
  }

  // Prepare submitting the form
  jQuery('#migrationForm').submit(function() {
    prepareSubmitChannels();
  });

  // Switch displayed channel tree
  jQuery('#base-channel-select').change(function() {
    showChannelTree(jQuery(this).val());
  });
});

