import React from 'react';
import SpaRenderer from "core/spa/spa-renderer";
import {RolesProvider} from "core/auth/roles-context";
import {UserLocalizationProvider} from "core/user-localization/user-localization-context"
import AddCluster from './add-cluster';

export const renderer = (id, {contentAdd, flashMessage} = {}) => {
  let providersJson = {};
  try{
    providersJson = JSON.parse(contentAdd);
  } catch(error) {
      console.log(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <AddCluster providers={providersJson} flashMessage={flashMessage}/>
      </UserLocalizationProvider>  
    </RolesProvider>,
    document.getElementById(id)
  );
 
};
