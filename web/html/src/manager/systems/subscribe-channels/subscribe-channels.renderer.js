/* global getServerId */
const React = require('react');
const ReactDOM = require('react-dom');
const { SubscribeChannels } = require('./subscribe-channels');
const SpaRenderer  = require("core/spa/spa-renderer").default;

SpaRenderer.renderNavigationReact(
  <SubscribeChannels
    serverId={getServerId()}
  />,
  document.getElementById('subscribe-channels-div'),
);
