/* eslint-disable */
"use strict";

function showDialog(dialogId) {
  $('#' + dialogId).modal('show');
}

function hideDialog(dialogId) {
  $('#' + dialogId).modal('hide');
}

module.exports = {
    showDialog,
    hideDialog
};
