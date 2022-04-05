import SpaRenderer from "core/spa/spa-renderer";

import { ProxyConfig } from "./container-config";

export const renderer = (id: string) => {
  return SpaRenderer.renderNavigationReact(<ProxyConfig />, document.getElementById(id));
};
