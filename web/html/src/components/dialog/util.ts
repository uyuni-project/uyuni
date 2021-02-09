export function showDialog(dialogId: string) {
  jQuery("#" + dialogId).modal("show");
}

export function hideDialog(dialogId: string) {
  jQuery("#" + dialogId).modal("hide");
}
