import React from 'react';
import PackageStates from './package-states';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, {serverId}) => {
  SpaRenderer.renderNavigationReact(
    <PackageStates
      serverId={serverId}
    />,
    document.getElementById(id)
  );
};
