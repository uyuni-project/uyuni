import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import { UserLocalizationProvider } from "core/user-localization/user-localization-context";

import { ServerMessageType } from "components/messages";
import { MessagesContainer } from "components/toastr/toastr";

import UpgradeCluster from "./upgrade-cluster";

type RendererProps = {
  cluster?: string;
  showUpgradePlan?: boolean;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { cluster, showUpgradePlan, flashMessage }: RendererProps = {}) => {
  let clusterObj: any = {};
  try {
    clusterObj = JSON.parse(cluster || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <UserLocalizationProvider>
        <MessagesContainer />
        <UpgradeCluster cluster={clusterObj} showUpgradePlan={showUpgradePlan} flashMessage={flashMessage} />
      </UserLocalizationProvider>
    </RolesProvider>,
    document.getElementById(id)
  );
};
