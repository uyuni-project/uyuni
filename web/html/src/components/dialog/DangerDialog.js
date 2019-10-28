/* eslint-disable */
import PropTypes  from 'prop-types';
import React from "react";
import { Button } from "../buttons";
import { Dialog } from "./Dialog";

/**
 * A pop-up dialog for dangerous actions confirmation.
 * It contains a 'Cancel' button and a button with text from 'submitText'
 * and icon from 'submitIcon'.
 * Related data may be passed with the 'item' property.
 * This 'item' will be passed to the 'onConfirm' and 'onClosePopUp' handlers.
 */
export function DangerDialog(props) {
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
          onClosePopUp={props.onClosePopUp}
          />
    );
}

DangerDialog.propTypes = {
    id: PropTypes.string.isRequired,
    onClosePopUp: PropTypes.func,
    className: PropTypes.string,
    title: PropTypes.string,
    content: PropTypes.node,
    item: PropTypes.object,
    onConfirm: PropTypes.func,
    submitText: PropTypes.string,
    submitIcon: PropTypes.string,
};
