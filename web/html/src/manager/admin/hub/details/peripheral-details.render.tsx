import SpaRenderer from "core/spa/spa-renderer";

import { Channel, Org, SyncedCustomChannels } from "components/hub";

import IssPeripheralDetails from "./peripheral-details";

export const renderer = (
  id: string,
  peripheralOrgs: Org[],
  syncedPeripheralCustomChannels: SyncedCustomChannels,
  syncedPeripheralVendorChannels: Channel[],
  availableCustomChannels: Channel[],
  availableVendorChannels: Channel[]
) => {
  SpaRenderer.renderNavigationReact(
    <IssPeripheralDetails
      availableOrgs={peripheralOrgs}
      availableCustomChannels={availableCustomChannels}
      availableVendorChannels={availableVendorChannels}
      syncedCustomChannels={syncedPeripheralCustomChannels}
      syncedVendorChannels={syncedPeripheralVendorChannels}
    />,
    document.getElementById(id)
  );
};
