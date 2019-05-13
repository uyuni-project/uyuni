import React from 'react';
import ReactDOM from 'react-dom';
import ListFilters from './list-filters';
import "./list-filters.css";
import {RolesProvider} from "core/auth/roles-context";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.contentManagement = window.pageRenderers.contentManagement || {};
window.pageRenderers.contentManagement.listFilters = window.pageRenderers.contentManagement.listFilters || {};
window.pageRenderers.contentManagement.listFilters.renderer = (id, {filters, projectLabel, openFilterId, flashMessage}) => {

  console.log(openFilterId);

  let filtersJson = [];
  try{
    filtersJson = JSON.parse(filters);
  }  catch(error) {}

  ReactDOM.render(
    <RolesProvider>
      <ListFilters
        filters={filtersJson}
        openFilterId={openFilterId}
        projectLabel={projectLabel}
        flashMessage={flashMessage}
      />
    </RolesProvider>,
    document.getElementById(id),
  );
};
