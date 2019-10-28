const SpaRenderer  = require("core/spa/spa-renderer").default;
const React = require('react');
const { GuestsCreate } = require('./guests-create');

export const renderer = (id, {
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
