// Event handler methods
function initChannelSelect() {
  var baseChannelSelect = $('base-channel-select');
  if (baseChannelSelect != null) {
    $('base-channel-select').selectedIndex = 0;
  }
}
// Show child channels of selected base channel
function showChannelTree(channelID) {
  $$('div.channels-tree').each(function(element) {
    if (element.id == 'channels-tree-' + channelID) {
      element.show();
    } else {
      element.hide();
    }
  });
}
// Create hidden inputs to submit channel IDs
function prepareSubmitChannels() {
  $('migrationForm').getInputs('hidden', 'childChannels[]').each(
    function(element) {
      element.remove();
    }
  );
  // Submit all checked child channel's IDs
  $$('div.channels-tree').each(function(element) {
    if (element.visible()) {
      element.select('input').each(function(checkbox) {
        if (checkbox.checked == true) {
          $('migrationForm').appendChild(
            createHiddenInput('childChannels[]', checkbox.value));
        }
      });
    }
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

