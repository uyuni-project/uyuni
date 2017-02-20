"use strict";

const React = require("react");

const DateTime = props =>
    <span title={moment(props.time).format("LL")}>
        {moment(props.time).fromNow()}
    </span>
;

module.exports = {
    DateTime: DateTime
}