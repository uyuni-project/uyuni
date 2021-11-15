import * as React from "react";
import { PopUp } from "../popup";

declare global {
  interface JQuery {
    modal: (command: "show" | "hide") => JQuery;
  }
}

export function openLegacyDialog(dialogId: string) {
  jQuery("#" + dialogId).modal("show");
}

export function closeLegacyDialog(dialogId: string) {
  jQuery("#" + dialogId).modal("hide");
}

export type LegacyDialogProps = {
  id: string;
  className?: string;
  title?: React.ReactNode;
  content?: React.ReactNode;
  buttons?: React.ReactNode;
  closableModal?: boolean;
  onClosePopUp?: (...args: any[]) => any;
};

export function LegacyDialog(props: LegacyDialogProps) {
  const { onClosePopUp, buttons, ...rest } = props;

  return <PopUp footer={buttons} onClosePopUp={() => onClosePopUp?.()} {...rest} />;
}
