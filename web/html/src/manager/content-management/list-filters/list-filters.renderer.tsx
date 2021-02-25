import * as React from "react";
import ListFilters from "./list-filters";
import "./list-filters.css";
import { RolesProvider } from "core/auth/roles-context";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";
import SpaRenderer from "core/spa/spa-renderer";
import { MessagesContainer } from "components/toastr/toastr";
import { ServerMessageType } from "components/messages";

type RendererProps = {
  filters?: string;
  projectLabel?: string;
  openFilterId?: number;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { filters, projectLabel, openFilterId, flashMessage }: RendererProps = {}) => {
  let filtersJson = [];
  try {
    filtersJson = JSON.parse(filters || "");
  } catch (error) {
    console.log(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer />
        <ListFilters
          filters={filtersJson}
          openFilterId={openFilterId}
          projectLabel={projectLabel}
          flashMessage={flashMessage}
        />
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
};
