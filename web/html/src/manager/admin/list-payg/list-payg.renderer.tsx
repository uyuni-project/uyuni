import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { ServerMessageType } from "components/messages/messages";
import { MessagesContainer } from "components/toastr/toastr";

import ListPayg from "./list-payg";

type RendererProps = {
  payg_instances?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { payg_instances, flashMessage }: RendererProps = {}) => {
  let projectsJson: any[] = [];
  try {
    projectsJson = JSON.parse(payg_instances || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <ListPayg payg_instances={projectsJson} flashMessage={flashMessage} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
