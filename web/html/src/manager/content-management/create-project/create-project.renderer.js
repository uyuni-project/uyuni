import React from 'react';
import ReactDOM from 'react-dom';
import CreateProject from './create-project';
import {RolesProvider} from "core/auth/roles-context";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.createProject = window.pageRenderers.contentManagement.createProject || {};
window.pageRenderers.contentManagement.createProject.renderer = (id) => {
  ReactDOM.render(
    <RolesProvider>
      <CreateProject />
    </RolesProvider>,
    document.getElementById(id),
  );
};
