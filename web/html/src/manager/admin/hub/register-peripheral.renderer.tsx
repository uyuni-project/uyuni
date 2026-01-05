import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { RegisterPeripheralForm } from "components/hub";
import { MessagesContainer } from "components/toastr";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <RegisterPeripheralForm />
    </RolesProvider>,
    document.getElementById(id)
  );
};
