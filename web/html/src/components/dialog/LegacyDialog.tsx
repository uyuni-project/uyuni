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
  footer?: React.ReactNode;
  closableModal?: boolean;
  onClosePopUp?: () => void;
};

export function LegacyDialog(props: LegacyDialogProps) {
  const { onClosePopUp, footer, ...rest } = props;

  return <PopUp footer={footer} onClosePopUp={() => onClosePopUp?.()} {...rest} />;
}
