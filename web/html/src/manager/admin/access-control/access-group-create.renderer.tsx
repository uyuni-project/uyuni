import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import AccessGroup from "./access-group";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<AccessGroup />, document.getElementById(id));
};
