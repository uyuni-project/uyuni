import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { RolesProvider } from "core/auth/roles-context";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";
import { MessagesContainer } from "components/toastr/toastr";
import RemoveNode from "./remove-node";
import { ServerMessageType } from "components/messages";

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
    console.log(error);
  }

  let nodesObj: any = {};
  try {
    nodesObj = JSON.parse(nodes || "");
  } catch (error) {
    console.log(error);
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
