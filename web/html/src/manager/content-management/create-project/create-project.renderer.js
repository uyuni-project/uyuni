import React from 'react';
import CreateProject from './create-project';
import {RolesProvider} from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.createProject = window.pageRenderers.contentManagement.createProject || {};
window.pageRenderers.contentManagement.createProject.renderer = (id) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <CreateProject />
    </RolesProvider>,
    document.getElementById(id),
  );
};
