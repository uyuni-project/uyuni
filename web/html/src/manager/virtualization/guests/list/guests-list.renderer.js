// @flow

const ReactDOM = require('react-dom');
const React = require('react');
const { GuestsList } = require('./guests-list');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.list = window.pageRenderers.guests.list || {};
window.pageRenderers.guests.list.renderer = (id, { serverId, saltEntitled, isAdmin }) => {
  const element = document.getElementById(id);
  if (element !== null) {
    ReactDOM.render(
      <GuestsList
        refreshInterval={5 * 1000}
        serverId={serverId}
        saltEntitled={saltEntitled}
        isAdmin={isAdmin}
      />,
      element,
    );
  }
};
