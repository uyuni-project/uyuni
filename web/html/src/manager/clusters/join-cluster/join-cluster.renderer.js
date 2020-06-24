import React from 'react';
import SpaRenderer from "core/spa/spa-renderer";
import {RolesProvider} from "core/auth/roles-context";
import {UserLocalizationProvider} from "core/user-localization/user-localization-context"
import JoinCluster from './join-cluster';
import {MessagesContainer} from 'components/toastr/toastr';

export const renderer = (id, {cluster, flashMessage} = {}) => {

  let clusterJson = {};
  try{
    clusterJson = JSON.parse(cluster);
  } catch(error) {
      console.log(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer/>
        <JoinCluster cluster={clusterJson} flashMessage={flashMessage}/>
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
 
};
