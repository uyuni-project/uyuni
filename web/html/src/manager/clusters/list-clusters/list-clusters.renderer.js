import React from 'react';
import ListClusters from './list-clusters';
import {RolesProvider} from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import {MessagesContainer} from 'components/toastr/toastr';

export const renderer = (id, {clusters, flashMessage}) => {

  let clustersJson = [];
  try{
    clustersJson = JSON.parse(clusters);
  }  catch(error) {}

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer/>
      <ListClusters clusters={clustersJson} flashMessage={flashMessage}/>
    </RolesProvider>,
    document.getElementById(id)
  );
 
};