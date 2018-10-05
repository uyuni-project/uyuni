/* eslint-disable */
"use strict";

const React = require("react");

const DateTime = props =>
    <span title={moment(props.time).format("YYYY-MM-DD HH:mm:ss Z")}>
        {moment(props.time).fromNow()}
    </span>
;

module.exports = {
    DateTime: DateTime
}
