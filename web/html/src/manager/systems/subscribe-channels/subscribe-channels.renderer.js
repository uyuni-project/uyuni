/* global getServerId */
const React = require('react');
const ReactDOM = require('react-dom');
const { SubscribeChannels } = require('./subscribe-channels');

ReactDOM.render(
  <SubscribeChannels
    serverId={getServerId()}
  />,
  document.getElementById('subscribe-channels-div'),
);
