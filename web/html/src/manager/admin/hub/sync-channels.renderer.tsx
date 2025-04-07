import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { Channel, FlatChannel, Org, SyncOrgsToPeripheralChannel } from "components/hub";
import { MessagesContainer } from "components/toastr";

export type ChannelSyncProps = {
  peripheralOrgs: Org[];
  syncedPeripheralCustomChannels: Channel[];
  syncedPeripheralVendorChannels: Channel[];
  availableCustomChannels: Channel[];
  availableVendorChannels: Channel[];
};

/**
 * Converts a hierarchical array of Channel objects to a flat array of FlatChannel objects.
 * The resulting array includes all channels and their children, with parent-child
 * relationships preserved through the childrenIds property.
 *
 * @param channels - An array of hierarchical Channel objects
 * @returns An array of FlatChannel objects
 */
export function flattenChannels(channels: Channel[], isSynced: boolean): FlatChannel[] {
  const flatChannels: FlatChannel[] = [];
  /**
   * Process a channel and its children recursively, adding them to the flat array
   * @param channel - The current channel to process
   */
  const processChannel = (channel: Channel): void => {
    // Extract child labels
    const childrenLabels = channel.children.map((child) => child.channelLabel);
    // Create a FlatChannel from the current channel
    const flatChannel: FlatChannel = {
      channelId: channel.channelId,
      channelName: channel.channelName,
      channelLabel: channel.channelLabel,
      channelArch: channel.channelArch,
      channelOrg: channel.channelOrg,
      parentChannelLabel: channel.parentChannelLabel,
      childrenLabels: childrenLabels,
      synced: isSynced,
    };
    // Add the flat channel to our result array
    flatChannels.push(flatChannel);
    // Process all children recursively
    channel.children.forEach((child) => processChannel(child));
  };
  // Process all root channels and their children
  channels.forEach((channel) => processChannel(channel));
  return flatChannels;
}

export const renderer = (
  id: string,
  peripheralId: number,
  peripheralFqdn: string,
  channelsSyncData: ChannelSyncProps
) => {
  const flatAvailableCustom = flattenChannels(channelsSyncData.availableCustomChannels, false);
  const flatAvailableVendor = flattenChannels(channelsSyncData.availableVendorChannels, false);
  const flatSyncedCustom = flattenChannels(channelsSyncData.syncedPeripheralCustomChannels, true);
  const flatSyncedVendor = flattenChannels(channelsSyncData.syncedPeripheralVendorChannels, true);
  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <SyncOrgsToPeripheralChannel
        peripheralId={peripheralId}
        peripheralFqdn={peripheralFqdn}
        availableOrgs={channelsSyncData.peripheralOrgs}
        availableCustomChannels={flatAvailableCustom}
        availableVendorChannels={flatAvailableVendor}
        syncedCustomChannels={flatSyncedCustom}
        syncedVendorChannels={flatSyncedVendor}
      />
    </RolesProvider>,
    document.getElementById(id)
  );
};
