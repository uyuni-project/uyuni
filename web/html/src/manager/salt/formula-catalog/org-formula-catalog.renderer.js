import React from 'react';
import ReactDOM from 'react-dom';
import FormulaCatalog from './org-formula-catalog';

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.salt = window.pageRenderers.salt || {};
window.pageRenderers.salt.formulaCatalog = window.pageRenderers.salt.formulaCatalog || {};
window.pageRenderers.salt.formulaCatalog.renderer = (id, {flashMessage, warningMessage}) => {

  ReactDOM.render(
    <FormulaCatalog
      flashMessage={flashMessage}
      warningMessage={warningMessage}
    />,
    document.getElementById(id)
  );

};
