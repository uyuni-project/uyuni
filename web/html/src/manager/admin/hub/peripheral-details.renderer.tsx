import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralDetailData } from "components/hub";
import { MessagesContainer } from "components/toastr";

import { PeripheralDetails } from "./peripheral-details";

export const renderer = (id: string, peripheralDetailsData: PeripheralDetailData) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <PeripheralDetails peripheral={peripheralDetailsData} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
