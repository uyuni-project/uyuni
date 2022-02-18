import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import CreateProject from "./create-project";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <CreateProject />
    </RolesProvider>,
    document.getElementById(id)
  );
};
