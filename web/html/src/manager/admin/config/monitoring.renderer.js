import React from 'react';
import ReactDOM from 'react-dom';
import MonitoringAdmin from './monitoring';

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.admin = window.pageRenderers.admin || {};
window.pageRenderers.admin.monitoring = window.pageRenderers.admin.monitoring || {};
window.pageRenderers.admin.monitoring.renderer = (id) => {

  ReactDOM.render(
    <MonitoringAdmin />,
    document.getElementById(id)
  );
 
};

