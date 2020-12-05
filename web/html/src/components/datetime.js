/* eslint-disable */
"use strict";

import React from 'react';

const DateTime = props =>
    <span title={moment(props.time).format("YYYY-MM-DD HH:mm:ss Z")}>
        {moment(props.time).fromNow()}
    </span>
;

module.exports = {
    DateTime: DateTime
}
