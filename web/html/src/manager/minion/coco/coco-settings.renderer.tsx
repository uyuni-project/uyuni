import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import CoCoSettings from "./coco-settings";

// See java/code/src/com/suse/manager/webui/templates/minion/coco-settings.jade
declare global {
  interface Window {
    serverId?: any;
    actionChains?: any;
    availableEnvironmentTypes?: any;
  }
}

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <CoCoSettings
      serverId={window.serverId}
      availableEnvironmentTypes={window.availableEnvironmentTypes ?? []}
      // TODO: enable when the backend implementation is ready
      showOnScheduleOption={false}
    />,
    document.getElementById(id)
  );
