import * as React from "react";
import { useEffect } from "react";

declare global {
  interface JQuery {
    modal: (command: "show" | "hide") => JQuery;
  }
}

export type LegacyDialogProps = {
  id: string;
  /** The css className for the 'modal-dialog' div */
  className?: string;
  title?: React.ReactNode;
  /** The body of the popup */
  content?: React.ReactNode;
  /** The footer of the popup */
  footer?: React.ReactNode;
  hideHeader?: boolean;
  closableModal?: boolean;
  /** Callback for when the dialog is closed */
  onClose?: () => void;
};

export function openLegacyDialog(dialogId: string) {
  jQuery("#" + dialogId).modal("show");
}

export function closeLegacyDialog(dialogId: string) {
  jQuery("#" + dialogId).modal("hide");
}

export const LegacyDialog = (props: LegacyDialogProps) => {
  useEffect(() => {
    if (props.onClose) {
      jQuery("#" + props.id).on("hidden.bs.modal", () => props.onClose?.());
    }
  }, []);

  let bootStrapModalOptionalProps: any = {};

  const closableModal = props.closableModal ?? true;

  if (!closableModal) {
    bootStrapModalOptionalProps = {
      ...bootStrapModalOptionalProps,
      "data-backdrop": "static",
      "data-keyboard": "false",
    };
  }

  return (
    <div className="modal fade" tabIndex="-1" role="dialog" id={props.id} {...bootStrapModalOptionalProps}>
      <div className={"modal-dialog " + (props.className ? props.className : "")}>
        <div className="modal-content">
          {!props.hideHeader && (
            <div className="modal-header">
              {closableModal && (
                <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                  <span aria-hidden="true">&times;</span>
                </button>
              )}
              {props.title ? <h4 className="modal-title">{props.title}</h4> : null}
            </div>
          )}
          <div className="modal-body">{props.content}</div>
          {props.footer ? <div className="modal-footer">{props.footer}</div> : null}
        </div>
      </div>
    </div>
  );
};
