import SpaRenderer from "core/spa/spa-renderer";

import { SystemData } from "components/target-systems";

import CoCoSSMSettings from "./ssm-settings";

// See java/code/src/com/suse/manager/webui/templates/ssm/coco-ssm-settings.jade
declare global {
  interface Window {
    systemSupport?: SystemData[];
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
