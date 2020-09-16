/* eslint-disable */
export function showDialog(dialogId) {
  jQuery('#' + dialogId).modal('show');
}

export function hideDialog(dialogId) {
  jQuery('#' + dialogId).modal('hide');
}
