import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import Project from "./project";

type RendererProps = {
  project?: string;
  wasFreshlyCreatedMessage?: string;
};

export const renderer = (id: string, { project, wasFreshlyCreatedMessage }: RendererProps = {}) => {
  let projectJson: any = {};
  try {
    projectJson = JSON.parse(project || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <Project project={projectJson} {...(wasFreshlyCreatedMessage && { wasFreshlyCreatedMessage })} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
