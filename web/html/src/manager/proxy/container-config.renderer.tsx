import SpaRenderer from "core/spa/spa-renderer";

import { ProxyConfig } from "./container-config";

export const renderer = (parent: Element) => {
  return SpaRenderer.renderNavigationReact(<ProxyConfig />, parent);
};
