'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const CustomStatesModule = require("../components/custom-states")
const CustomStates = CustomStatesModule.CustomStates;
const MessagesUtils = require("../components/messages").Utils;
const msg = CustomStatesModule.msg;
const Network = require("../utils/network");

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
              messages: MessagesUtils.info(t("Applying the custom states has been scheduled for each minion server in this group"))
          });
        });
}

function saveRequest(states) {
    return Network.post(
        "/rhn/manager/api/states/save",
        JSON.stringify({
             id: groupId,
             type: "GROUP",
             saltStates: states
         }),
        "application/json"
    );
}

ReactDOM.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
