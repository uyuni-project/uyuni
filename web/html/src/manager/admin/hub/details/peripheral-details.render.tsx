import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { Channel, Org, SyncOrgsToPeripheralChannel } from "components/hub";
import { MessagesContainer } from "components/toastr";

import PeripheralDetails from "./peripheral-details";

type PeripheralDetailsProps = {
  fqdn: string;
};

type ChannelSyncProps = {
  peripheralOrgs: Org[];
  syncedPeripheralCustomChannels: Channel[];
  syncedPeripheralVendorChannels: Channel[];
  availableCustomChannels: Channel[];
  availableVendorChannels: Channel[];
};

export const renderer = (
  id: string,
  detailsData: PeripheralDetailsProps | null,
  channelsSyncData: ChannelSyncProps
) => {
  // TODO: find a better way to get path parameters
  const pathname = window.location.pathname;
  const segments = pathname.split("/").filter(Boolean);
  const peripheralId = Number(segments[segments.length - 1]);
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <PeripheralDetails fqdn={""} />
      <SyncOrgsToPeripheralChannel
        peripheralId={peripheralId}
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
