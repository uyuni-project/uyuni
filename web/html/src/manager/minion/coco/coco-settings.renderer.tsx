import SpaRenderer from "core/spa/spa-renderer";

import { CoCoSettings } from "./coco-settings";

// See java/core/src/main/resources/com/suse/manager/webui/templates/minion/coco-settings.jade
export interface CocoSettingsProps {
  serverId: number;
  availableEnvironmentTypes: Record<string, string>;
}

export const renderer = (id: string, { serverId, availableEnvironmentTypes }: CocoSettingsProps) => {
  SpaRenderer.renderNavigationReact(
    <CoCoSettings
      serverId={serverId}
      availableEnvironmentTypes={availableEnvironmentTypes}
      // TODO: enable when the backend implementation is ready
      showOnScheduleOption={false}
    />,
    document.getElementById(id)
  );
};
