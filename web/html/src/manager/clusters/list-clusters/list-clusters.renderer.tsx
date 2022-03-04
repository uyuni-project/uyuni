import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { ServerMessageType } from "components/messages";
import { MessagesContainer } from "components/toastr/toastr";

import ListClusters from "./list-clusters";

type RendererProps = {
  clusters?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { clusters, flashMessage }: RendererProps = {}) => {
  let clustersJson = [];
  try {
    clustersJson = JSON.parse(clusters || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <ListClusters clusters={clustersJson} flashMessage={flashMessage} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
