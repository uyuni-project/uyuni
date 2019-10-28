import React from 'react';
import MonitoringAdmin from './monitoring-admin';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id) => {

  SpaRenderer.renderNavigationReact(
    <MonitoringAdmin />,
    document.getElementById(id)
  );
 
};
