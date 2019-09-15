import React from 'react';
import SpaRenderer from "core/spa/spa-renderer";
import FormulaCatalog from './org-formula-catalog';

export const renderer = (id, {flashMessage, warningMessage}) => {

  SpaRenderer.renderNavigationReact(
    <FormulaCatalog
      flashMessage={flashMessage}
      warningMessage={warningMessage}
    />,
    document.getElementById(id)
  );

};
