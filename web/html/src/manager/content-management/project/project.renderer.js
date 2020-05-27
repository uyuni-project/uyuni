import React from 'react';
import Project from './project';
import {RolesProvider} from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import {MessagesContainer} from 'components/toastr/toastr';

export const renderer = (id, {project, wasFreshlyCreatedMessage} = {}) => {
  let projectJson = {};
  try{
    projectJson = JSON.parse(project);
  }  catch(error) {}

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer/>
      <Project
        project={projectJson}
        { ...( wasFreshlyCreatedMessage && { wasFreshlyCreatedMessage } ) }
      />
    </RolesProvider>,
    document.getElementById(id),
  );
};
