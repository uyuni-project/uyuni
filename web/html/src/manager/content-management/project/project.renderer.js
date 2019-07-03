import React from 'react';
import ReactDOM from 'react-dom';
import Project from './project';
import {RolesProvider} from "core/auth/roles-context";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.project = window.pageRenderers.contentManagement.project || {};
window.pageRenderers.contentManagement.project.renderer = (id, {project, wasFreshlyCreatedMessage} = {}) => {
  let projectJson = {};
  try{
    projectJson = JSON.parse(project);
  }  catch(error) {}

  ReactDOM.render(
    <RolesProvider>
      <Project
        project={projectJson}
        { ...( wasFreshlyCreatedMessage && { wasFreshlyCreatedMessage } ) }
      />
    </RolesProvider>,
    document.getElementById(id),
  );
};
