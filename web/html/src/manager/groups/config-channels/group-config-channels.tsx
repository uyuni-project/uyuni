import * as React from "react";
import { ConfigChannels } from "components/config-channels";
import { Utils as MessagesUtils } from "components/messages";
import Network from "utils/network";
import SpaRenderer from "core/spa/spa-renderer";

// See java/build/classes/com/suse/manager/webui/templates/groups/custom.jade
declare global {
  interface Window {
    groupId?: any;
  }
}

function matchUrl(target) {
  return "/rhn/manager/api/states/match?id=" + window.groupId + "&type=GROUP" + (target ? "&target=" + target : "");
}

function applyRequest(component) {
  return Network.post(
    "/rhn/manager/api/states/apply",
    {
      id: window.groupId,
      type: "GROUP",
      states: ["custom_groups"],
    },
  ).then(data => {
    component.setState({
      messages: MessagesUtils.info(
        t("Applying the config channels has been scheduled for each minion server in this group")
      ),
    });
  });
}

function saveRequest(states) {
  return Network.post(
    "/rhn/manager/api/states/save",
    {
      id: window.groupId,
      type: "GROUP",
      channels: states,
    }
  );
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(
    <ConfigChannels matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest} />,
    document.getElementById("config-channels")
  );
