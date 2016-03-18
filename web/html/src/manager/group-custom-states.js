'use strict';

const React = require("react");
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
              messages: MessagesUtils.msg('info', <span>{t("Applying the custom states has been scheduled for each minion server in this group")}</span>)
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

React.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
