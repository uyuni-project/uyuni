export function showDialog(dialogId: string) {
  jQuery("#" + dialogId)
    .modal("show")
    // Here and below, these manual handlers offer compatibility between Bootstrap 3 and 5, we can drop these after the migration is complete
    .addClass("show");

  jQuery(".modal-backdrop.in").addClass("show");
}

export function hideDialog(dialogId: string) {
  jQuery("#" + dialogId)
    .modal("hide")
    .removeClass("show");

  if (jQuery(".modal.in").length === 0) {
    jQuery(".modal-backdrop.show").removeClass("show");
  }
}
