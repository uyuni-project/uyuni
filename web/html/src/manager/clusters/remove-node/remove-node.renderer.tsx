import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";

import { ServerMessageType } from "components/messages";
import { MessagesContainer } from "components/toastr/toastr";

import RemoveNode from "./remove-node";

type RendererProps = {
  cluster?: string;
  nodes?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { cluster, nodes, flashMessage }: RendererProps = {}) => {
  let clusterObj: any = {};
  try {
    clusterObj = JSON.parse(cluster || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  let nodesObj: any = {};
  try {
    nodesObj = JSON.parse(nodes || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer />
        <RemoveNode cluster={clusterObj} nodes={nodesObj} flashMessage={flashMessage} />
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
};
