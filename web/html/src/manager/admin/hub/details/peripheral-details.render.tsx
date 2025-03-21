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

export const renderer = (id: string, channelsSyncData: ChannelSyncProps) => {
  // TODO: find a better way to get path parameters
  const pathname = window.location.pathname;
  const segments = pathname.split("/").filter(Boolean);
  const peripheralId = Number(segments[segments.length - 1]);
  const mapAvailableChannels = (ch: Channel) => {
    ch.synced = false;
    return ch;
  }
  const mapSyncedChannels = (ch: Channel) => {
    ch.synced = true;
    return ch;
  };
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <SyncOrgsToPeripheralChannel
        peripheralId={peripheralId}
        availableOrgs={channelsSyncData.peripheralOrgs}
        availableCustomChannels={channelsSyncData.availableCustomChannels.map(mapAvailableChannels)}
        availableVendorChannels={channelsSyncData.availableVendorChannels.map(mapAvailableChannels)}
        syncedCustomChannels={channelsSyncData.syncedPeripheralCustomChannels.map(mapSyncedChannels)}
        syncedVendorChannels={channelsSyncData.syncedPeripheralVendorChannels.map(mapSyncedChannels)}
      />
    </RolesProvider>,
    document.getElementById(id)
  );
};
