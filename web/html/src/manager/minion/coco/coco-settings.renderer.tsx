import SpaRenderer from "core/spa/spa-renderer";

import { CoCoSettings } from "./coco-settings";

// See java/core/src/main/resources/com/suse/manager/webui/templates/minion/coco-settings.jade
export interface CocoSettingsProps {
  serverId: number;
  availableEnvironmentTypes: Record<string, string>;
}

type LegacyCocoSettingsGlobals = Partial<CocoSettingsProps>;

export const renderer = (id: string, props?: CocoSettingsProps) => {
  const legacyGlobals = window as Window & LegacyCocoSettingsGlobals;
  const serverId = props?.serverId ?? legacyGlobals.serverId;
  const availableEnvironmentTypes = props?.availableEnvironmentTypes ?? legacyGlobals.availableEnvironmentTypes;

  if (serverId === undefined || availableEnvironmentTypes === undefined) {
    return;
  }

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
