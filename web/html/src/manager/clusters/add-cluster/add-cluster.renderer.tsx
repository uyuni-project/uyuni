import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";

import { ServerMessageType } from "components/messages";
import { MessagesContainer } from "components/toastr/toastr";

import AddCluster from "./add-cluster";

type RendererProps = {
  contentAdd?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { contentAdd, flashMessage }: RendererProps = {}) => {
  let providersJson: any = {};
  try {
    providersJson = JSON.parse(contentAdd || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <UserLocalizationProvider>
        <AddCluster providers={providersJson} />
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
};
