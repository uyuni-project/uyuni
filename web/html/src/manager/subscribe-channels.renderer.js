/* global module, getServerId */
const { SubscribeChannels } = require('./subscribe-channels');
const ReactDOM = require("react-dom");
const React = require("react");

ReactDOM.render(
  <SubscribeChannels
    serverId={getServerId()}
  />,
  document.getElementById("subscribe-channels-div")
);
