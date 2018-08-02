"use strict";

const React = require("react");
const PropTypes = React.PropTypes;
const Button = require("../buttons").Button;
const showDialog = require("./util").showDialog;

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
    item: PropTypes.object,
    onClick: PropTypes.func
};

module.exports = {
    ModalButton
};
