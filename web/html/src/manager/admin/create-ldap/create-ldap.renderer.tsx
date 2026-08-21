import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import CreateLdap from "./create-ldap";

type RendererProps = {
  orgs?: string;
};

export const renderer = (id: string, { orgs }: RendererProps = {}) => {
  let orgsJson: any[] = [];
  try {
    orgsJson = JSON.parse(orgs || "[]");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <CreateLdap orgs={orgsJson} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
