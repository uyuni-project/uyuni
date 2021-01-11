/* eslint-disable */
import PropTypes  from 'prop-types';
import * as React from 'react';
import { AsyncButton, Button } from "../buttons";
import { Dialog } from "./Dialog";

/**
 * A pop-up dialog for dangerous actions confirmation.
 * It contains a 'Cancel' button and a button with text from 'submitText'
 * and icon from 'submitIcon'.
 * Related data may be passed with the 'item' property.
 * This 'item' will be passed to the 'onConfirm' and 'onClosePopUp' handlers.
 */
export function DangerDialog(props) {
    const btnClass = props.btnClass || "btn-danger";
    const buttons = <div>
        {props.onConfirmAsync ?
            <AsyncButton
                text={props.submitText}
                title={props.submitText}
                icon={props.submitIcon}
                defaultType={btnClass}
                action={() => {
                    props.onConfirmAsync(true);
                    jQuery('#' + props.id).modal('hide');
                }}
            /> : null
        }
        {props.onConfirm ?
            <Button
                className={btnClass}
                text={props.submitText}
                title={props.submitText}
                icon={props.submitIcon}
                handler={() => {
                    props.onConfirm(props.item);
                    jQuery('#' + props.id).modal('hide');
                }}
            /> : null
        }

        <Button
            className="btn-default"
            text={t("Cancel")}
            title={t("Cancel")}
            icon="fa-close"
            handler={() => {
                jQuery('#' + props.id).modal('hide');
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
    onConfirmAsync: PropTypes.func,
    submitText: PropTypes.string,
    submitIcon: PropTypes.string,
    btnClass: PropTypes.string
};
