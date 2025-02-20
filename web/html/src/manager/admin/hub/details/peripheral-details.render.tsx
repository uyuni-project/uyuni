import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { Channel, Org, SyncedCustomChannels, SyncOrgsToPeripheralChannel } from "components/hub";
import { MessagesContainer } from "components/toastr";

import IssPeripheralDetails from "./peripheral-details";
import PeripheralDetails from "./peripheral-details";

type PeripheralDetailsProps = {
  fqdn: string;
};

type ChannelSyncProps = {
  peripheralOrgs: Org[];
  syncedPeripheralCustomChannels: SyncedCustomChannels;
  syncedPeripheralVendorChannels: Channel[];
  availableCustomChannels: Channel[];
  availableVendorChannels: Channel[];
};

export const renderer = (
  id: string,
  detailsData: PeripheralDetailsProps | null,
  channelsSyncData: ChannelSyncProps
) => {
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <PeripheralDetails fqdn={""} />
      <SyncOrgsToPeripheralChannel
        availableOrgs={channelsSyncData.peripheralOrgs}
        availableCustomChannels={channelsSyncData.availableCustomChannels}
        availableVendorChannels={channelsSyncData.availableVendorChannels}
        syncedCustomChannels={channelsSyncData.syncedPeripheralCustomChannels}
        syncedVendorChannels={channelsSyncData.syncedPeripheralVendorChannels}
      />
    </RolesProvider>,
    document.getElementById(id)
  );
};
