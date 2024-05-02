import SpaRenderer from "core/spa/spa-renderer";

import { SystemData } from "components/target-systems";

import CoCoSSMSchedule from "./ssm-schedule";

// See java/code/src/com/suse/manager/webui/templates/ssm/coco-ssm-schedule.jade
declare global {
  interface Window {
    systemSupport?: SystemData[];
    actionChains?: any;
  }
}

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <CoCoSSMSchedule systemSupport={window.systemSupport ?? []} actionChains={window.actionChains} />,
    document.getElementById(id)
  );
