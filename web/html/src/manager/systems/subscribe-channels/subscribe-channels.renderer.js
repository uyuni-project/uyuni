/* global getServerId */
const React = require('react');
const { SubscribeChannels } = require('./subscribe-channels');
const SpaRenderer  = require("core/spa/spa-renderer").default;

export const renderer = (id) => SpaRenderer.renderNavigationReact(
  <SubscribeChannels
    serverId={getServerId()}
  />,
  document.getElementById(id),
);
