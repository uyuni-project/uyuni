import * as React from "react";
import { ConfigChannels } from "components/config-channels";
import { Utils as MessagesUtils } from "components/messages";
import Network from "utils/network";
import SpaRenderer from "core/spa/spa-renderer";

// See java/code/src/com/suse/manager/webui/templates/minion/custom.jade
declare global {
  interface Window {
    serverId?: any;
  }
}

function matchUrl(target?: string) {
  return "/rhn/manager/api/states/match?id=" + window.serverId + "&type=SERVER" + (target ? "&target=" + target : "");
}

function applyRequest(component) {
  return Network.post(
    "/rhn/manager/api/states/apply",
    JSON.stringify({
      id: window.serverId,
      type: "SERVER",
      states: ["custom"],
    })
  ).then(data => {
    component.setState({
      messages: MessagesUtils.info(
        <span>
          {t("Applying the config channels has been ")}
          <a href={"/rhn/systems/details/history/Event.do?sid=" + window.serverId + "&aid=" + data}>{t("scheduled")}</a>
        </span>
      ),
    });
  });
}

function saveRequest(states) {
  return Network.post(
    "/rhn/manager/api/states/save",
    JSON.stringify({
      id: window.serverId,
      type: "SERVER",
      channels: states,
    })
  );
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(
    <ConfigChannels matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest} />,
    document.getElementById("config-channels")
  );
