const React = require('react');
const ReactDOM = require('react-dom');
const { GuestsList } = require('./guests-list');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.list = window.pageRenderers.guests.list || {};
window.pageRenderers.guests.list.renderer = (id, { serverId, saltEntitled, isAdmin }) => {
  ReactDOM.render(
    <GuestsList
      refreshInterval={5 * 1000}
      serverId={serverId}
      saltEntitled={saltEntitled}
      isAdmin={isAdmin}
    />,
    document.getElementById(id),
  );
};
