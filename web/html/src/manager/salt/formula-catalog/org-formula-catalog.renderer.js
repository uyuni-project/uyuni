import React from 'react';
import SpaRenderer from "core/spa/spa-renderer";
import FormulaCatalog from './org-formula-catalog';

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.salt = window.pageRenderers.salt || {};
window.pageRenderers.salt.formulaCatalog = window.pageRenderers.salt.formulaCatalog || {};
window.pageRenderers.salt.formulaCatalog.renderer = (id, {flashMessage, warningMessage}) => {

  SpaRenderer.renderNavigationReact(
    <FormulaCatalog
      flashMessage={flashMessage}
      warningMessage={warningMessage}
    />,
    document.getElementById(id)
  );

};
