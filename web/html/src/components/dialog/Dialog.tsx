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
  const closeModalPromise = new Promise(resolve => {
    jQuery("#" + modalId).on("hidden.bs.modal", () => resolve(undefined));
  });
  jQuery("#" + modalId).modal("hide");
  return closeModalPromise;
}

// TODO: Define props
export type DialogProps = {
  id: string;
  className?: string;
  title?: React.ReactNode;
  content?: React.ReactNode;
  buttons?: React.ReactNode;
  item?: any;
  closableModal?: boolean;
  /** Whether to automatically focus the first input in the opened modal, true by default */
  autoFocus?: boolean;
  onClosePopUp?: (...args: any[]) => any;
  onConfirm?: (...args: any[]) => any;
  onConfirmAsync?: (...args: any[]) => Promise<any>;
};

export function Dialog(props: DialogProps) {
  const { onClosePopUp, buttons, ...OtherProps } = props;

  React.useEffect(() => {
    if (props.autoFocus === false) {
      return;
    }
    jQuery("#" + props.id).on("shown.bs.modal", function() {
      jQuery("#" + props.id + " :input:visible:enabled:first").focus();
    });
  }, []);

  return <PopUp footer={buttons} onClosePopUp={() => onClosePopUp?.()} {...OtherProps} />;
}
