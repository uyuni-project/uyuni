import { TokenList } from "manager/admin/hub/list-tokens";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <TokenList />
    </RolesProvider>,
    document.getElementById(id)
  );
};
