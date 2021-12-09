import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { RolesProvider } from "core/auth/roles-context";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";
import { MessagesContainer } from "components/toastr/toastr";

import AddCluster from "./add-cluster";
import { ServerMessageType } from "components/messages";

type RendererProps = {
  contentAdd?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { contentAdd, flashMessage }: RendererProps = {}) => {
  let providersJson: any = {};
  try {
    providersJson = JSON.parse(contentAdd || "");
  } catch (error) {
    console.log(error);
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
