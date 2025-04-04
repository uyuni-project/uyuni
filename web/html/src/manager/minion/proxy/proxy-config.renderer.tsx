import SpaRenderer from "core/spa/spa-renderer";

import { ProxyConfig } from "./proxy-config";

export const renderer = (
  id: string,
  {
    serverId,
    isUyuni,
    parents,
    currentConfig,
    initFailMessage,
  }: { serverId: string; isUyuni: boolean; parents: any[]; currentConfig: any; initFailMessage: string }
) => {
  return SpaRenderer.renderNavigationReact(
    <ProxyConfig
      serverId={serverId}
      isUyuni={isUyuni}
      parents={parents}
      currentConfig={currentConfig}
      initFailMessage={initFailMessage}
    />,
    document.getElementById(id)
  );
};
