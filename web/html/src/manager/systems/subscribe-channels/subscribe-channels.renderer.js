const React = require('react');
const { SubscribeChannels } = require('./subscribe-channels');
const SpaRenderer  = require("core/spa/spa-renderer").default;

export const renderer = (id, {systemId}) => SpaRenderer.renderNavigationReact(
  <SubscribeChannels serverId={systemId} />,
  document.getElementById(id),
);
