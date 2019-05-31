import React from 'react';
import MonitoringAdmin from './monitoring-admin';
import SpaRenderer from "core/spa/spa-renderer";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.admin = window.pageRenderers.admin || {};
window.pageRenderers.admin.monitoring = window.pageRenderers.admin.monitoring || {};
window.pageRenderers.admin.monitoring.renderer = (id) => {

  SpaRenderer.renderNavigationReact(
    <MonitoringAdmin />,
    document.getElementById(id)
  );
 
};
