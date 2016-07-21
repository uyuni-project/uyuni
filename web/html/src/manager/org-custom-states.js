'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const CustomStatesModule = require("../components/custom-states")
const CustomStates = CustomStatesModule.CustomStates;
const MessagesUtils = require("../components/messages").Utils;
const msg = CustomStatesModule.msg;
const Network = require("../utils/network");

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
            messages: MessagesUtils.info(t("Applying the custom states has been scheduled for each minion server in this organization"))
        });
    });
}

function saveRequest(states) {
    return Network.post(
        "/rhn/manager/api/states/save",
        JSON.stringify({
            id: orgId,
            type: "ORG",
            saltStates: states
        }),
        "application/json"
    );
}

ReactDOM.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
