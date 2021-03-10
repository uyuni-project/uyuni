import * as React from "react";
import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import { MessagesContainer } from "components/toastr/toastr";
import { ServerMessageType } from "components/messages";

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
    console.log(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <ListClusters clusters={clustersJson} flashMessage={flashMessage} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
