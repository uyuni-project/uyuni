/* global module */
const React = require('react');
const ReactDOM = require('react-dom');
const { GuestsCreate } = require('./guests-create');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.create = window.pageRenderers.guests.create || {};
window.pageRenderers.guests.create.guestsCreateRenderer = (id, {
  host,
  timezone,
  localTime,
  actionChains,
}) => {
  ReactDOM.render(
    <GuestsCreate
      host={host}
      localTime={localTime}
      timezone={timezone}
      actionChains={actionChains}
    />,
    document.getElementById(id),
  );
};
