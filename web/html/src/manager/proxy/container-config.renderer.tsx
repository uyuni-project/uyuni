import SpaRenderer from "core/spa/spa-renderer";

import { ProxyType } from "../systems/proxy";
import { ProxyConfig } from "./container-config";

export const renderer = (id: string, noSSL: boolean, parents: ProxyType[] = []) => {
  return SpaRenderer.renderNavigationReact(
    <ProxyConfig noSSL={noSSL} parents={parents} />,
    document.getElementById(id)
  );
};
