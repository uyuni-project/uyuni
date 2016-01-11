$(function() {
  // Show child channels of selected base channel
  function showChannelTree(channelID) {
    $('.channels-tree#channels-tree-' + channelID).show();
    $('.channels-tree[id != channels-tree-' + channelID + ']').hide();
  }

  // Create hidden inputs to submit channel IDs
  function prepareSubmitChannels() {
    $('#migrationForm input[type="hidden", name="childChannels"]').remove();
    // Submit all checked child channel's IDs
    $('.channels-tree:visible input:checked').each(function() {
      $('#migrationForm').append(
        createHiddenInput('childChannels', $(this).val())
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
  $('#migrationForm').submit(function() {
    prepareSubmitChannels();
  });

  // Switch displayed channel tree
  $('#base-channel-select').change(function() {
    showChannelTree($(this).val());
  });
});

