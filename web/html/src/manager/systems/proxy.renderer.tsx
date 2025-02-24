import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Proxy } from "./proxy";

export const renderer = (parent: Element) =>
  SpaRenderer.renderNavigationReact(<Proxy proxies={window.proxies} currentProxy={window.currentProxy} />, parent);
