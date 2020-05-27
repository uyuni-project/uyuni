import React from 'react';
import PackageStates from './package-states';
import SpaRenderer from "core/spa/spa-renderer";
import {MessagesContainer} from "components/toastr/toastr";

export const renderer = (id, {serverId}) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer/>
      <PackageStates
        serverId={serverId}
      />
    </>,
    document.getElementById(id)
  );
};
