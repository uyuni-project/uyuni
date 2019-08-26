/* eslint-disable */
export function showDialog(dialogId) {
  $('#' + dialogId).modal('show');
}

export function hideDialog(dialogId) {
  $('#' + dialogId).modal('hide');
}
