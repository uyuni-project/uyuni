"use strict";

const React = require("react");
const Button = require("./buttons").Button;
const PopUp = require("./popup").PopUp;

/**
 * Button to launch a modal dialog
 */
function ModalButton(props) {
    return (
        <Button
            id={props.id}
            className={props.className}
            title={props.title}
            text={props.text}
            icon={props.icon}
            handler={ () => {
                if(props.onClick) props.onClick(props.item);
                $('#' + props.target).modal('show');
            }}
        />
    );
}

ModalButton.propTypes = {
    target: React.PropTypes.string.isRequired,
    id: React.PropTypes.string,
    className: React.PropTypes.string,
    title: React.PropTypes.string,
    text: React.PropTypes.string,
    icon: React.PropTypes.string,
    item: React.PropTypes.object,
    onClick: React.PropTypes.func
};

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
        <PopUp
            id={props.id}
            className={props.className}
            content={props.content}
            title={props.title}
            footer={buttons}
            onClosePopUp={() => props.onClosePopUp && props.onClosePopUp(props.item)}
        />
    );
}

DeleteDialog.propTypes = {
    id: React.PropTypes.string.isRequired,
    onClosePopUp: React.PropTypes.func,
    className: React.PropTypes.string,
    title: React.PropTypes.string,
    content: React.PropTypes.node,
    item: React.PropTypes.object,
    onConfirm: React.PropTypes.func
};

module.exports = {
    ModalButton: ModalButton,
    DeleteDialog: DeleteDialog
};
