import React from 'react';
import ReactDOM from 'react-dom';
import ListProjects from './list-projects';
import {RolesProvider} from "core/auth/roles-context";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.listProjects = window.pageRenderers.contentManagement.listProjects || {};
window.pageRenderers.contentManagement.listProjects.renderer = (id, {projects, flashMessage}) => {

  let projectsJson = [];
  try{
    projectsJson = JSON.parse(projects);
  }  catch(error) {}

  ReactDOM.render(
    <RolesProvider>
      <ListProjects
        projects={projectsJson}
        flashMessage={flashMessage}
      />
    </RolesProvider>,
    document.getElementById(id),
  );
};
