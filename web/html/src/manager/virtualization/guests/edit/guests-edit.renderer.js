// @flow

const React = require('react');
const ReactDOM = require('react-dom');
const { GuestsEdit } = require('./guests-edit');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.edit = window.pageRenderers.guests.edit || {};
window.pageRenderers.guests.edit.guestsEditRenderer = (id, { host, guest }) => {
  const element = document.getElementById(id);
  if (element !== null) {
    ReactDOM.render(
      <GuestsEdit
        guest={guest}
        host={host}
      />,
      element,
    );
  }
};
