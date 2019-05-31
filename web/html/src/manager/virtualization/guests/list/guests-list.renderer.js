const SpaRenderer  = require("core/spa/spa-renderer").default;
const React = require('react');
const ReactDOM = require('react-dom');
const { GuestsList } = require('./guests-list');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.list = window.pageRenderers.guests.list || {};
window.pageRenderers.guests.list.renderer = (id, { serverId, saltEntitled, foreignEntitled, isAdmin }) => {
  SpaRenderer.renderNavigationReact(
    <GuestsList
      refreshInterval={5 * 1000}
      serverId={serverId}
      saltEntitled={saltEntitled}
      foreignEntitled={foreignEntitled}
      isAdmin={isAdmin}
    />,
    document.getElementById(id),
  );
};
