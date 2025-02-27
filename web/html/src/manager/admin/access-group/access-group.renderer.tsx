import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import CreateProjects from "./access-group";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <CreateProjects />
    </RolesProvider>,
    document.getElementById(id)
  );
};
