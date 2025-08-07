import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import AccessGroup, { AccessGroupState } from "./access-group";

export const renderer = (id: string, accessGroup: AccessGroupState) => {
  SpaRenderer.renderNavigationReact(<AccessGroup accessGroup={accessGroup} />, document.getElementById(id));
};
