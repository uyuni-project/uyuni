"use strict";

const React = require("react");
const PropTypes = React.PropTypes;
const showDialog = require("./util").showDialog;

/**
 * Link to launch a modal dialog
 */
function ModalLink(props) {
    const margin = props.text != undefined ? "" : " no-margin"
    var icon = props.icon && <i className={'fa ' + props.icon + margin}/>;
    return (
        <a
            id={props.id}
            href="#"
            title={props.title}
            className={props.className}
            onClick={ () => {
                if(props.onClick) props.onClick(props.item);
                showDialog(props.target);
            }}
        >
            {icon}
            {props.text}
        </a>
    );
}

ModalLink.propTypes = {
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
    ModalLink
};
