"use strict";

function showDialog(dialogId) {
  $('#' + dialogId).modal('show');
}

function hideDialog(modalId) {
  $('#' + dialogId).modal('hide');
}

module.exports = {
    showDialog,
    hideDialog
};
