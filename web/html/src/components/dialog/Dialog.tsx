import * as React from "react";
import { PopUp } from "../popup";

// TODO: Move to popup once that gets migrated
type Instance = JQuery & {
  modal(...args: any[]): any;
};
type Modal = ((...args: any[]) => Instance);

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

export function Dialog(props) {
  const { onClosePopUp, buttons, ...OtherProps } = props;

  React.useEffect(() => {
    jQuery("#" + props.id).on("shown.bs.modal", function() {
      jQuery("#" + props.id + " :input:visible:enabled:first").focus();
    });
  }, []);

  return <PopUp footer={buttons} onClosePopUp={() => onClosePopUp && onClosePopUp()} {...OtherProps} />;
}
