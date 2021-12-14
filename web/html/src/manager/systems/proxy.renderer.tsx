import * as React from "react";
import { Proxy } from "./proxy";
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <Proxy proxies={window.proxies} currentProxy={window.currentProxy} />,
    document.getElementById(id)
  );
