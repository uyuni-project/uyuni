/* eslint-disable */
'use strict';

// TODO rename this to panels.js
// TODO use the same syntax (ES5 or ES6) in all classes

var React = require("react");

function SmallPanel(props) {
    return (
        <div className="panel panel-default">
            { props.title &&
            <div className="panel-heading">
                <h4>
                  { props.icon && <i className={"fa " + props.icon}></i> }
                  {props.title}
                  { props.editUrl && <span> (<a href={props.editUrl}>{t("Edit")}</a>)</span> }
                </h4>
            </div>
            }
            <div className="panel-body">
                { props.children }
            </div>
        </div>
    );
}

module.exports = {
    SmallPanel: SmallPanel
}
