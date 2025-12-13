import SpaRenderer from "core/spa/spa-renderer";

import CoCoSSMSchedule from "./ssm-schedule";
import { CoCoSystemData } from "./types";

// See java/core/src/main/resources/com/suse/manager/webui/templates/ssm/coco-ssm-schedule.jade
declare global {
  interface Window {
    systemSupport?: CoCoSystemData[];
    actionChains?: any;
  }
}

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <CoCoSSMSchedule systemSupport={window.systemSupport ?? []} actionChains={window.actionChains} />,
    document.getElementById(id)
  );
