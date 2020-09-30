const SpaRenderer  = require("core/spa/spa-renderer").default;
const React = require('react');
const { GuestsCreate } = require('./guests-create');

export const renderer = (id, {
  host,
  timezone,
  localTime,
  actionChains,
  cobblerProfiles,
}) => {
  SpaRenderer.renderNavigationReact(
    <GuestsCreate
      host={host}
      localTime={localTime}
      timezone={timezone}
      actionChains={actionChains}
      cobblerProfiles={cobblerProfiles}
    />,
    document.getElementById(id),
  );
};
