import React from 'react';
import CreateProject from './create-project';
import {RolesProvider} from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <CreateProject />
    </RolesProvider>,
    document.getElementById(id),
  );
};
