/* eslint-disable */
// @flow
"use strict";

const {GuestsEdit} = require("./guests-edit");
const React = require("react");
const ReactDOM = require("react-dom");

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.edit = window.pageRenderers.guests.edit || {};
window.pageRenderers.guests.edit.guestsEditRenderer = (id, {host, guest}) => ReactDOM.render(
  <GuestsEdit
    guest={guest}
    host={host}
  />,
  document.getElementById(id)
);
