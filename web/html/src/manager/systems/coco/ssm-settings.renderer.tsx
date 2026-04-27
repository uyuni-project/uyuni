import SpaRenderer from "core/spa/spa-renderer";

import CoCoSSMSettings from "./ssm-settings";
import { CoCoSystemData } from "./types";

// See java/core/src/main/resources/com/suse/manager/webui/templates/ssm/coco-ssm-settings.jade
interface CoCoSSMSettingsProps {
  systemSupport: CoCoSystemData[];
  availableEnvironmentTypes: Record<string, string>;
}

export const renderer = (id, { systemSupport, availableEnvironmentTypes }: CoCoSSMSettingsProps) =>
  SpaRenderer.renderNavigationReact(
    <CoCoSSMSettings systemSupport={systemSupport} availableEnvironmentTypes={availableEnvironmentTypes} />,
    document.getElementById(id)
  );
