/* eslint-disable */
'use strict';

import * as React from 'react';
import ReactDOM from 'react-dom';
import ConfigChannelsModule from 'components/config-channels';
const ConfigChannels = ConfigChannelsModule.ConfigChannels;
import { Utils as MessagesUtils } from 'components/messages';
import Network from 'utils/network';
import SpaRenderer from 'core/spa/spa-renderer';

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

export const renderer = () => SpaRenderer.renderNavigationReact(
  <ConfigChannels matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('config-channels')
);
