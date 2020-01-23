/* eslint-disable */
"use strict";

const PropTypes = require('prop-types');
const React = require("react");

const StatePersistedContext = React.createContext({loadState: undefined, saveState: undefined});

module.exports = {
    StatePersistedContext,
}
