export function showDialog(dialogId: string) {
  // The base event is "hide.bs.modal", we only want to remove the listener we added so we add a namespace, see https://api.jquery.com/event.namespace/
  const namespacedEventName = "hide.bs.modal.namespace-dialog-util";

  const dialog = jQuery("#" + dialogId)
    .modal("show")
    // Here and below, these manual handlers offer compatibility between Bootstrap 3 and 5, we can drop these after the migration is complete
    .addClass("show")
    .off(namespacedEventName)
    .on(namespacedEventName, () => {
      dialog.removeClass("show").off(namespacedEventName);

      if (jQuery(".modal.in").length === 0) {
        jQuery(".modal-backdrop.show").removeClass("show");
      }
    });

  jQuery(".modal-backdrop.in").addClass("show");
}

export function hideDialog(dialogId: string) {
  jQuery("#" + dialogId).modal("hide");
}
