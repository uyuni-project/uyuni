const SpaRenderer  = require("core/spa/spa-renderer").default;
const React = require('react');
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
  SpaRenderer.renderNavigationReact(
    <GuestsCreate
      host={host}
      localTime={localTime}
      timezone={timezone}
      actionChains={actionChains}
    />,
    document.getElementById(id),
  );
};
