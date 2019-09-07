const React = require('react');
const { GuestsEdit } = require('./guests-edit');
const SpaRenderer  = require("core/spa/spa-renderer").default;

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.edit = window.pageRenderers.guests.edit || {};
window.pageRenderers.guests.edit.guestsEditRenderer = (id, {
  host,
  guestUuid,
  timezone,
  localTime,
  actionChains,
}) => {
  SpaRenderer.renderNavigationReact(
    <GuestsEdit
      host={host}
      guestUuid={guestUuid}
      timezone={timezone}
      localTime={localTime}
      actionChains={actionChains}
    />,
    document.getElementById(id),
  );
};
