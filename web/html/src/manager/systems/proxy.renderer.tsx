import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Proxy } from "./proxy";

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <Proxy proxies={window.proxies} currentProxy={window.currentProxy} />,
    document.getElementById(id)
  );
