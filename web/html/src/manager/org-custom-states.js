'use strict';

const React = require("react");
const CustomStatesModule = require("../components/custom-states")
const CustomStates = CustomStatesModule.CustomStates;
const msg = CustomStatesModule.msg;
const Network = require("../utils/network");

function matchUrl(target) {
    return "/rhn/manager/api/states/match?id=" + orgId + "&type=org"
             + (target ? "&target=" + target : "");
}

function applyRequest(component) {
    return Network.post(
        "/rhn/manager/api/states/apply",
        JSON.stringify({
             id: orgId,
             type: "org",
             states: ["custom_org"]
         }),
        "application/json",
        data => {
          console.log("apply action queued:" + data)
          component.setState({
              messages: msg('info', <span>{t("Applying the custom states has been scheduled for each minion server in this organization")}</span>)
          });
        }
    );
}

function saveRequest(states, doneCallback, failCallback) {
    return Network.post(
        "/rhn/manager/api/states/save",
        JSON.stringify({
            id: orgId,
            type: "org",
            saltStates: states
        }),
        "application/json",
        doneCallback,
        failCallback
    );
}

React.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest} a/>,
  document.getElementById('custom-states')
);
