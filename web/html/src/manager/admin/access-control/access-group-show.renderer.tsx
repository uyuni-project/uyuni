import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import AccessGroup, { AccessGroupPropsType } from "./access-group";

export const renderer = (id: string, accessGroup: AccessGroupPropsType) => {
  SpaRenderer.renderNavigationReact(<AccessGroup accessGroup={accessGroup} />, document.getElementById(id));
};
