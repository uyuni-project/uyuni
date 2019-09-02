import React from 'react';
import Project from './project';
import {RolesProvider} from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.project = window.pageRenderers.contentManagement.project || {};
window.pageRenderers.contentManagement.project.renderer = (id, {project, wasFreshlyCreatedMessage} = {}) => {
  let projectJson = {};
  try{
    projectJson = JSON.parse(project);
  }  catch(error) {}

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <Project
        project={projectJson}
        { ...( wasFreshlyCreatedMessage && { wasFreshlyCreatedMessage } ) }
      />
    </RolesProvider>,
    document.getElementById(id),
  );
};
