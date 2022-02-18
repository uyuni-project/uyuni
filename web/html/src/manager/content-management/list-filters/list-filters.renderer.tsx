import "./list-filters.css";

import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";

import { MessagesContainer } from "components/toastr/toastr";

import ListFilters from "./list-filters";

export const renderer = (id, { filters, flashMessage }) => {
  let filtersJson = [];
  try {
    filtersJson = JSON.parse(filters);
  } catch (error) {
    console.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer />
        <ListFilters filters={filtersJson} flashMessage={flashMessage} />
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
};
