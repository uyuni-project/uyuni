import React from 'react';
import ListProjects from './list-projects';
import {RolesProvider} from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.listProjects = window.pageRenderers.contentManagement.listProjects || {};
window.pageRenderers.contentManagement.listProjects.renderer = (id, {projects, flashMessage}) => {

  let projectsJson = [];
  try{
    projectsJson = JSON.parse(projects);
  }  catch(error) {}

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <ListProjects
        projects={projectsJson}
        flashMessage={flashMessage}
      />
    </RolesProvider>,
    document.getElementById(id),
  );
};
