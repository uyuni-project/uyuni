import * as React from "react";
import ReactModal from "react-modal";

export type DialogProps = {
  /** whether the dialog should be shown or hidden */
  isOpen: boolean;
  /** Function to call to close the modal. This typically changes the value of isOpen */
  onClose?: () => void;
  /** The id of the html div tag */
  id: string;
  /** The css className for the 'modal-dialog' div */
  className?: string;
  /** Title of the dialog */
  title?: React.ReactNode;
  /** The body of the popup */
  content?: React.ReactNode;
  /** The footer content of the dialog. Usually buttons. */
  footer?: React.ReactNode;
  /** Should the dialog show a header with a title. Defaults to False */
  hideHeader?: boolean;
  /** Can the modal be closed using the cross button, ESC or by clicking on the overlay? */
  closableModal?: boolean;
};

ReactModal.setAppElement(document.body);

export function Dialog(props: DialogProps) {
  const closableModal = props.closableModal ?? true;

  return (
    <ReactModal
      isOpen={props.isOpen}
      id={props.id}
      overlayClassName="modal-overlay"
      className="modal-dialog react-modal"
      shouldCloseOnOverlayClick={closableModal}
      shouldCloseOnEsc={closableModal}
      onRequestClose={() => {
        props.onClose?.();
      }}
    >
      <div className="modal-content">
        <div className="modal-content">
          {!props.hideHeader && (
            <div className="modal-header">
              {closableModal && (
                <button
                  type="button"
                  className="close"
                  data-dismiss="modal"
                  aria-label="Close"
                  onClick={() => props.onClose?.()}
                >
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
    </ReactModal>
  );
}
