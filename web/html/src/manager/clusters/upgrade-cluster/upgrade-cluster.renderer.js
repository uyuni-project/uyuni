import React from 'react';
import SpaRenderer from "core/spa/spa-renderer";
import {RolesProvider} from "core/auth/roles-context";
import {UserLocalizationProvider} from "core/user-localization/user-localization-context"
import {MessagesContainer} from 'components/toastr/toastr';
import UpgradeCluster from './upgrade-cluster';

export const renderer = (id, {cluster, flashMessage} = {}) => {

  let clusterObj = {};
  try {
    clusterObj = JSON.parse(cluster);
  } catch(error) {
    console.log(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer/>
        <UpgradeCluster cluster={clusterObj} flashMessage={flashMessage}/>
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );

};
