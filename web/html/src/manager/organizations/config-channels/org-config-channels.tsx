import * as React from "react";
import { ConfigChannels } from "components/config-channels";
import { Utils as MessagesUtils } from "components/messages";
import Network from "utils/network";
import SpaRenderer from "core/spa/spa-renderer";

// See java/code/src/com/suse/manager/webui/templates/yourorg/custom.jade
declare global {
  interface Window {
    orgId?: any;
  }
}

function matchUrl(target?: string) {
  return "/rhn/manager/api/states/match?id=" + window.orgId + "&type=ORG" + (target ? "&target=" + target : "");
}

function applyRequest(component) {
  return Network.post("/rhn/manager/api/states/apply", {
    id: window.orgId,
    type: "ORG",
    states: ["custom_org"],
  }).then((data) => {
    console.log("apply action queued:" + data);
    component.setState({
      messages: MessagesUtils.info(
        t("Applying the config channels has been scheduled for each minion server in this organization")
      ),
    });
  });
}

function saveRequest(states) {
  return Network.post("/rhn/manager/api/states/save", {
    id: window.orgId,
    type: "ORG",
    channels: states,
  });
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(
    <ConfigChannels matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest} />,
    document.getElementById("config-channels")
  );
