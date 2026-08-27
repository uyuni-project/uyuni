import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import Ldap from "./ldap";

type RendererProps = {
  ldap?: string;
  orgs?: string;
  wasFreshlyCreatedMessage?: string;
};

export const renderer = (id: string, { ldap, orgs, wasFreshlyCreatedMessage }: RendererProps = {}) => {
  let ldapJson: any = null;
  let orgsJson: any[] = [];
  try {
    ldapJson = JSON.parse(ldap || "null");
  } catch (error) {
    Loggerhead.error(error);
  }
  try {
    orgsJson = JSON.parse(orgs || "[]");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <Ldap ldap={ldapJson} orgs={orgsJson} wasFreshlyCreatedMessage={wasFreshlyCreatedMessage} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
