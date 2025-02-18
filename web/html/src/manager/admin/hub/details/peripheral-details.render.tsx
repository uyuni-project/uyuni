import SpaRenderer from "core/spa/spa-renderer";

import IssPeripheralDetails from "./peripheral-details";

export const renderer = (id: string, hubOrgs: Org[], syncedPeripheralChannels: , availablePeripheralChannels) => {
  SpaRenderer.renderNavigationReact(<IssPeripheralDetails />, document.getElementById(id));
};
