/* eslint-disable */
import PropTypes from 'prop-types';
import React from "react";
import { Button } from "../buttons";
import { showDialog } from "./util";

/**
 * Button to launch a modal dialog
 */
export function ModalButton(props) {
    return (
        <Button
            id={props.id}
            className={props.className}
            title={props.title}
            text={props.text}
            icon={props.icon}
            disabled={props.disabled}
            handler={ () => {
                if(props.onClick) props.onClick(props.item);
                showDialog(props.target);
            }}
        />
    );
}

ModalButton.propTypes = {
    target: PropTypes.string.isRequired,
    id: PropTypes.string,
    className: PropTypes.string,
    title: PropTypes.string,
    text: PropTypes.string,
    icon: PropTypes.string,
    disabled: PropTypes.bool,
    item: PropTypes.object,
    onClick: PropTypes.func
};
