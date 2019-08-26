import React from "react";
import { PopUp } from "../popup";

export function closeDialog(modalId: string) {
  const closeModalPromise = new Promise((resolve) => {
    $('#' + modalId).on('hidden.bs.modal', () => resolve());
  });
  $('#' + modalId).modal('hide');
  return closeModalPromise;
}

export function Dialog(props) {
  const {onClosePopUp, buttons, ...OtherProps} = props;

  React.useEffect(() => {
    $('#' + props.id).on('shown.bs.modal', function () {
      $('#' + props.id + ' :input:visible:enabled:first').focus();
    })
  }, [])

  return (
      <PopUp
          footer={buttons}
          onClosePopUp={() => onClosePopUp && onClosePopUp()}
          {...OtherProps}
      />
  );
}
