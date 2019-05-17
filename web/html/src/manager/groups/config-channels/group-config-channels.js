/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const ConfigChannelsModule = require("components/config-channels")
const ConfigChannels = ConfigChannelsModule.ConfigChannels;
const MessagesUtils = require("components/messages").Utils;
const msg = ConfigChannelsModule.msg;
const Network = require("utils/network");

function matchUrl(target) {
    return "/rhn/manager/api/states/match?id=" + groupId + "&type=GROUP"
             + (target ? "&target=" + target : "");
}

function applyRequest(component) {
    return Network.post(
        "/rhn/manager/api/states/apply",
        JSON.stringify({
            id: groupId,
            type: "GROUP",
            states: ["custom_groups"]
        }),
        "application/json"
        )
        .promise.then( data => {
          console.log("apply action queued:" + data)
          component.setState({
              messages: MessagesUtils.info(t("Applying the config channels has been scheduled for each minion server in this group"))
          });
        });
}

function saveRequest(states) {
    return Network.post(
        "/rhn/manager/api/states/save",
        JSON.stringify({
             id: groupId,
             type: "GROUP",
             channels: states
         }),
        "application/json"
    );
}

ReactDOM.render(
  <ConfigChannels matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('config-channels')
);
