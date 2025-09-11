import SpaRenderer from "core/spa/spa-renderer";

import CoCoSSMSettings from "./ssm-settings";
import { CoCoSystemData } from "./types";

// See java/code/src/com/suse/manager/webui/templates/ssm/coco-ssm-settings.jade
declare global {
  interface Window {
    systemSupport?: CoCoSystemData[];
    availableEnvironmentTypes?: any;
  }
}

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <CoCoSSMSettings
      systemSupport={window.systemSupport ?? []}
      availableEnvironmentTypes={window.availableEnvironmentTypes ?? []}
    />,
    document.getElementById(id)
  );
