"use strict";

const React = require("react");
const PropTypes = React.PropTypes;
const Button = require("../buttons").Button;
const Dialog = require("./Dialog.js").Dialog;

/**
 * A pop-up dialog for dangerous actions confirmation.
 * It contains a 'Cancel' button and a button with text from 'submitText'
 * and icon from 'submitIcon'.
 * Related data may be passed with the 'item' property.
 * This 'item' will be passed to the 'onConfirm' and 'onClosePopUp' handlers.
 */
function DangerDialog(props) {
    const buttons = <div>
        <Button
            className="btn-danger"
            text={props.submitText}
            title={props.submitText}
            icon={props.submitIcon}
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

DangerDialog.propTypes = {
    id: React.PropTypes.string.isRequired,
    onClosePopUp: React.PropTypes.func,
    className: React.PropTypes.string,
    title: React.PropTypes.string,
    content: React.PropTypes.node,
    item: React.PropTypes.object,
    onConfirm: React.PropTypes.func,
    submitText: React.PropTypes.string,
    submitIcon: React.PropTypes.string,
};

module.exports = {
    DangerDialog: DangerDialog,
};
