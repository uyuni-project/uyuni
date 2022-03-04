import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";

import { ServerMessageType } from "components/messages";
import { MessagesContainer } from "components/toastr/toastr";

import JoinCluster from "./join-cluster";

type RendererProps = {
  cluster?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { cluster, flashMessage }: RendererProps = {}) => {
  let clusterJson: any = {};
  try {
    clusterJson = JSON.parse(cluster || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer />
        <JoinCluster cluster={clusterJson} flashMessage={flashMessage} />
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
};
