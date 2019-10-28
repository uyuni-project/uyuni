const React = require('react');
const { GuestsEdit } = require('./guests-edit');
const SpaRenderer  = require("core/spa/spa-renderer").default;

export const renderer = (id, {
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
