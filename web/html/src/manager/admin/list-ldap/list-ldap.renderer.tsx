import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { ServerMessageType } from "components/messages/messages";
import { MessagesContainer } from "components/toastr/toastr";

import ListLdap from "./list-ldap";

type RendererProps = {
  ldap_servers?: string;
  flashMessage?: ServerMessageType;
};

export const renderer = (id: string, { ldap_servers, flashMessage }: RendererProps = {}) => {
  let serversJson: any[] = [];
  try {
    serversJson = JSON.parse(ldap_servers || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <ListLdap ldap_servers={serversJson} flashMessage={flashMessage} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
