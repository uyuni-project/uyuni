import SpaRenderer from "core/spa/spa-renderer";

import { ProxyConfig } from "./container-config";

export const renderer = (id: string, noSSL: boolean, parents: any[] = []) => {
  return SpaRenderer.renderNavigationReact(
    <ProxyConfig noSSL={noSSL} parents={parents} />,
    document.getElementById(id)
  );
};
