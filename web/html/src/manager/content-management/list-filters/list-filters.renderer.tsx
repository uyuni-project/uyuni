import * as React from "react";
import ListFilters from "./list-filters";
import "./list-filters.css";
import { RolesProvider } from "core/auth/roles-context";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";
import SpaRenderer from "core/spa/spa-renderer";
import { MessagesContainer } from "components/toastr/toastr";

export const renderer = (id, { filters, flashMessage }) => {
  let filtersJson = [];
  try {
    filtersJson = JSON.parse(filters);
  } catch (error) {
    console.log(error);
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
