import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import CoCoScansList from "components/CoCoScansList";

// See java/code/src/com/suse/manager/webui/templates/minion/coco-scans-list.jade
declare global {
  interface Window {
    serverId?: any;
    actionChains?: any;
  }
}

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <CoCoScansList serverId={window.serverId} actionChains={window.actionChains} />,
    document.getElementById(id)
  );
