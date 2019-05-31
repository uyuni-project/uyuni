/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const ConfigChannelsModule = require("components/config-channels")
const ConfigChannels = ConfigChannelsModule.ConfigChannels;
const MessagesUtils = require("components/messages").Utils;
const Network = require("utils/network");
const SpaRenderer  = require("core/spa/spa-renderer").default;

function matchUrl(target) {
    return "/rhn/manager/api/states/match?id=" + serverId + "&type=SERVER"
             + (target ? "&target=" + target : "");
}

function applyRequest(component) {
    return Network.post(
        "/rhn/manager/api/states/apply",
        JSON.stringify({
            id: serverId,
            type: "SERVER",
            states: ["custom"]
        }),
        "application/json"
        )
        .promise.then(data => {
              console.log("apply action queued:" + data);
              component.setState({
                  messages: MessagesUtils.info(<span>{t("Applying the config channels has been ")}
                      <a href={"/rhn/systems/details/history/Event.do?sid=" + serverId + "&aid=" + data}>{t("scheduled")}</a>
                  </span>)
              });
        });
}

function saveRequest(states) {
    return Network.post(
        "/rhn/manager/api/states/save",
        JSON.stringify({
            id: serverId,
            type: "SERVER",
            channels: states
        }),
        "application/json"
    );
}

SpaRenderer.renderNavigationReact(
  <ConfigChannels matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('config-channels')
);
