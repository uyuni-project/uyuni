import * as React from "react";
import { PopUp } from "../popup";

// TODO: Move to popup once that gets migrated
type Instance = JQuery & {
  modal(...args: any[]): any;
};
type Modal = (...args: any[]) => Instance;

declare global {
  interface JQuery {
    modal: Modal;
  }
}

export function closeDialog(modalId: string) {
  const closeModalPromise = new Promise((resolve) => {
    jQuery("#" + modalId).on("hidden.bs.modal", () => resolve(undefined));
  });
  jQuery("#" + modalId).modal("hide");
  return closeModalPromise;
}

export type DialogProps = {
  id: string;
  className?: string;
  title?: React.ReactNode;
  content?: React.ReactNode;
  buttons?: React.ReactNode;
  closableModal?: boolean;
  onClosePopUp?: (...args: any[]) => any;
};

export function Dialog(props: DialogProps) {
  const { onClosePopUp, buttons, ...OtherProps } = props;

  return <PopUp footer={buttons} onClosePopUp={() => onClosePopUp?.()} {...OtherProps} />;
}
