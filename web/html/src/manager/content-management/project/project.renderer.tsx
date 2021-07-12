import * as React from "react";
import Project from "./project";
import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";
import { MessagesContainer } from "components/toastr";

type RendererProps = {
  project?: string;
  wasFreshlyCreatedMessage?: string;
};

export const renderer = (id: string, { project, wasFreshlyCreatedMessage }: RendererProps = {}) => {
  let projectJson: any = {};
  try {
    projectJson = JSON.parse(project || "");
  } catch (error) {
    console.log(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <Project project={projectJson} {...(wasFreshlyCreatedMessage && { wasFreshlyCreatedMessage })} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
