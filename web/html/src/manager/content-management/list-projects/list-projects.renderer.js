import React from 'react';
import ListProjects from './list-projects';
import {RolesProvider} from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import {MessagesContainer} from 'components/toastr/toastr';

export const renderer = (id, {projects, flashMessage}) => {

  let projectsJson = [];
  try{
    projectsJson = JSON.parse(projects);
  }  catch(error) {}

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer/>
      <ListProjects
        projects={projectsJson}
        flashMessage={flashMessage}
      />
    </RolesProvider>,
    document.getElementById(id),
  );
};
