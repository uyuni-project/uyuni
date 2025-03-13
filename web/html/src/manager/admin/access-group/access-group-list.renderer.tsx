import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";
import { AccessGroupList } from "./access-group-list";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    <AccessGroupList />,
    document.getElementById(id)
  );
};
