import React from 'react';
import ListFilters from './list-filters';
import "./list-filters.css";
import {RolesProvider} from "core/auth/roles-context";
import {UserLocalizationProvider} from "core/user-localization/user-localization-context";
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, {filters, projectLabel, openFilterId, flashMessage}) => {

  let filtersJson = [];
  try{
    filtersJson = JSON.parse(filters);
  }  catch(error) {}

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <ListFilters
          filters={filtersJson}
          openFilterId={openFilterId}
          projectLabel={projectLabel}
          flashMessage={flashMessage}
        />
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id),
  );
};
