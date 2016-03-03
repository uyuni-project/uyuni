'use strict';

const React = require("react");
const CustomStates = require("../components/custom-states").CustomStates;
const Network = require("../utils/network");

function matchUrl(target) {
    return "/rhn/manager/api/states/match?id=" + serverId + "&type=server"
             + (target ? "&target=" + target : "");
}

function applyRequest() {
    Network.post(
        "/rhn/manager/api/states/apply",
        JSON.stringify({
            id: serverId,
            type: "server",
            states: ["custom"]
        }),
        "application/json"
    );
}

function saveRequest(states) {
    const request = Network.post(
        "/rhn/manager/api/states/save",
        JSON.stringify({
            id: serverId,
            type: "server",
            saltStates: states
        }),
        "application/json"
    )
}

React.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
