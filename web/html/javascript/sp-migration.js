// Event handler methods
function initChannelSelect() {
  $('#base-channel-select').val(0);
}
// Show child channels of selected base channel
function showChannelTree(channelID) {
  $('.channels-tree#channels-tree-' + channelID).show();
  $('.channels-tree[id != channels-tree-' + channelID + ']').hide();
}
// Create hidden inputs to submit channel IDs
function prepareSubmitChannels() {
  $('#migrationForm input[type="hidden", name="childChannels[]"]').remove();
  // Submit all checked child channel's IDs
  $('.channels-tree:visible input:checked').each(function(checkbox) {
    $('#migrationForm').append(
      createHiddenInput('childChannels[]', checkbox.val())
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

$(function() {
  initChannelSelect();

  $('#migrationForm').submit(function() {
    prepareSubmitChannels();
  });

  $('#base-channel-select').change(function() {
    showChannelTree($(this).val());
  });
});