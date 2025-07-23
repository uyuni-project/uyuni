import SpaRenderer from "core/spa/spa-renderer";

import { ProxyConfig } from "./proxy-config";

export const renderer = (
  id: string,
  {
    serverId,
    parents,
    currentConfig,
    initFailMessage,
    registryUrlExample,
    registryTagExample,
    hasCertificates,
  }: {
    serverId: string;
    parents: any[];
    currentConfig: any;
    initFailMessage: string;
    registryUrlExample: string;
    registryTagExample: string;
    hasCertificates?: boolean;
  }
) => {
  return SpaRenderer.renderNavigationReact(
    <ProxyConfig
      serverId={serverId}
      parents={parents}
      currentConfig={currentConfig}
      initFailMessage={initFailMessage}
      registryUrlExample={registryUrlExample}
      registryTagExample={registryTagExample}
      hasCertificates={hasCertificates}
    />,
    document.getElementById(id)
  );
};
