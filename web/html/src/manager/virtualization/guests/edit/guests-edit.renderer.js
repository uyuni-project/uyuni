const React = require('react');
const ReactDOM = require('react-dom');
const { GuestsEdit } = require('./guests-edit');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.edit = window.pageRenderers.guests.edit || {};
window.pageRenderers.guests.edit.guestsEditRenderer = (id, { host, guestUuid }) => {
  ReactDOM.render(
    <GuestsEdit
      host={host}
      guestUuid={guestUuid}
    />,
    document.getElementById(id),
  );
};
