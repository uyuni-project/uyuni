import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { ServerMessageType } from "components/messages/messages";
import { MessagesContainer } from "components/toastr/toastr";

import ListProjects from "./list-projects";

type RendererProps = {
  projects?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { projects, flashMessage }: RendererProps = {}) => {
  let projectsJson: any[] = [];
  try {
    projectsJson = JSON.parse(projects || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <ListProjects projects={projectsJson} flashMessage={flashMessage} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
