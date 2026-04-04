import SpaRenderer from "core/spa/spa-renderer";

import { ProxyConfig } from "./container-config";

export const renderer = (id: string, noSSL: boolean) => {
  return SpaRenderer.renderNavigationReact(<ProxyConfig noSSL={noSSL} />, document.getElementById(id));
};
