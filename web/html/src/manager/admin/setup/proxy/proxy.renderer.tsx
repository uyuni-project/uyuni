import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import Proxy from "./proxy";
import { ProxySettings } from "./proxy-settings";

type Props = {
  proxySettings: ProxySettings;
};

export const renderer = (id: string, props: Props) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <Proxy proxySettings={props.proxySettings} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
