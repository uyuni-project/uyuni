"use strict";

const React = require("react");
const PropTypes = React.PropTypes;
const Button = require("../buttons").Button;
const Dialog = require("./Dialog").Dialog;

/**
 * A pop-up dialog for delete confirmation.
 * It contains a 'Delete' and a 'Cancel' button.
 * Related data may be passed with the 'item' property.
 * This 'item' will be passed to the 'onConfirm' and 'onClosePopUp' handlers.
 */
function DeleteDialog(props) {
    const buttons = <div>
        <Button
            className="btn-danger"
            text={t("Delete")}
            title={t("Delete")}
            icon="fa-trash"
            handler={() => {
                if(props.onConfirm) props.onConfirm(props.item);
                $('#' + props.id).modal('hide');
            }}
        />
        <Button
            className="btn-default"
            text={t("Cancel")}
            title={t("Cancel")}
            icon="fa-close"
            handler={() => {
                $('#' + props.id).modal('hide');
            }}
        />
    </div>;

    return (
        <Dialog
          id={props.id}
          className={props.className}
          content={props.content}
          title={props.title}
          buttons={buttons}
          />
    );
}

DeleteDialog.propTypes = {
    id: PropTypes.string.isRequired,
    onClosePopUp: PropTypes.func,
    className: PropTypes.string,
    title: PropTypes.string,
    content: PropTypes.node,
    item: PropTypes.object,
    onConfirm: PropTypes.func
};

module.exports = {
    DeleteDialog
};
