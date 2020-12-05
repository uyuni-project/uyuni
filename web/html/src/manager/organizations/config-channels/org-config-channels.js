/* eslint-disable */
'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import ConfigChannelsModule from 'components/config-channels';
const ConfigChannels = ConfigChannelsModule.ConfigChannels;
import { Utils as MessagesUtils } from 'components/messages';
const msg = ConfigChannelsModule.msg;
import Network from 'utils/network';
import SpaRenderer from 'core/spa/spa-renderer';

function matchUrl(target) {
    return "/rhn/manager/api/states/match?id=" + orgId + "&type=ORG"
             + (target ? "&target=" + target : "");
}

function applyRequest(component) {
    return Network.post(
        "/rhn/manager/api/states/apply",
        JSON.stringify({
             id: orgId,
             type: "ORG",
             states: ["custom_org"]
         }),
        "application/json"
    )
    .promise.then(data => {
        console.log("apply action queued:" + data)
        component.setState({
            messages: MessagesUtils.info(t("Applying the config channels has been scheduled for each minion server in this organization"))
        });
    });
}

function saveRequest(states) {
    return Network.post(
        "/rhn/manager/api/states/save",
        JSON.stringify({
            id: orgId,
            type: "ORG",
            channels: states
        }),
        "application/json"
    );
}

export const renderer = () => SpaRenderer.renderNavigationReact(
  <ConfigChannels matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('config-channels')
);
