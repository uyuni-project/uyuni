import React from 'react';
import SpaRenderer from "core/spa/spa-renderer";
import {RolesProvider} from "core/auth/roles-context";
import {UserLocalizationProvider} from "core/user-localization/user-localization-context"
import {MessagesContainer} from 'components/toastr/toastr';
import RemoveNode from './remove-node';

export const renderer = (id, {cluster, nodes, flashMessage} = {}) => {

  let clusterObj = {};
  try {
    clusterObj = JSON.parse(cluster);
  } catch(error) {
    console.log(error);
  }

  let nodesObj = {};
  try {
    nodesObj = JSON.parse(nodes);
  } catch(error) {
    console.log(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer/>
        <RemoveNode cluster={clusterObj} nodes={nodesObj} flashMessage={flashMessage}/>
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
 
};
