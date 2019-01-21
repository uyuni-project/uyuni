import React from 'react';
import ReactDOM from 'react-dom';
import Project from './project';

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.project = window.pageRenderers.contentManagement.project || {};
window.pageRenderers.contentManagement.project.renderer = (id) => {
  ReactDOM.render(
    <Project />,
    document.getElementById(id),
  );
};
