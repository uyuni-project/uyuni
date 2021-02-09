import * as React from "react";
import { DangerDialog } from "./DangerDialog";
import { DialogProps } from "./Dialog";

/**
 * A pop-up dialog for delete confirmation.
 * It contains a 'Delete' and a 'Cancel' button.
 * Related data may be passed with the 'item' property.
 * This 'item' will be passed to the 'onConfirm' and 'onClosePopUp' handlers.
 */
export function DeleteDialog(props: DialogProps) {
  return (
    <DangerDialog
      id={props.id}
      onClosePopUp={props.onClosePopUp}
      className={props.className}
      title={props.title}
      content={props.content}
      item={props.item}
      onConfirm={props.onConfirm}
      onConfirmAsync={props.onConfirmAsync}
      submitText={t("Delete")}
      submitIcon="fa-trash"
    />
  );
}
