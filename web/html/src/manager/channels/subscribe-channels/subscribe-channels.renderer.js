/* global module, getServerId */
const ReactDOM = require('react-dom');
const React = require('react');
const { SubscribeChannels } = require('./subscribe-channels');

ReactDOM.render(
  <SubscribeChannels
    serverId={getServerId()}
  />,
  document.getElementById('subscribe-channels-div'),
);
