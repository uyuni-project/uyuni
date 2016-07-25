'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const CustomStatesModule = require("../components/custom-states")
const CustomStates = CustomStatesModule.CustomStates;
const MessagesUtils = require("../components/messages").Utils;
const Network = require("../utils/network");

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
                  messages: MessagesUtils.info(<span>{t("Applying the custom states has been ")}
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
            saltStates: states
        }),
        "application/json"
    );
}

ReactDOM.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
