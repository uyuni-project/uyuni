const React = require("react");
const PopUp = require("../popup").PopUp;

function closeDialog(modalId: string) {
  const closeModalPromise = new Promise((resolve) => {
    $('#' + modalId).on('hidden.bs.modal', () => resolve());
  });
  $('#' + modalId).modal('hide');
  return closeModalPromise;
}

function Dialog(props) {
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

module.exports = {
    Dialog,
    closeDialog
};
