import { Channel, Org } from "./types";

export type FlatChannel = {
  channelId: number;
  channelName: string;
  channelLabel: string;
  channelArch: string;
  channelOrg: Org | null;
  parentChannelLabel: string | null; // if null or undefined, this is a root channel
  originalChannelLabel: string | null; // the id of the channel that this is a clone of
  childrenLabels: string[];
  synced: boolean;
};

/**
 * Converts hierarchical Channel structure to flat FlatChannel structure
 */
export function flattenChannels(hierarchicalChannels: Channel[]): FlatChannel[] {
  const flatChannels: FlatChannel[] = [];
  const channelMap = new Map<string, FlatChannel>();

  // First pass: create flat channels without childrenIds
  const processChannel = (channel: Channel, parentLabel: string | null = null) => {
    // Skip if we've already processed this channel
    if (channelMap.has(channel.channelLabel)) {
      return;
    }

    // Create a flat channel object
    const flatChannel: FlatChannel = {
      channelId: channel.channelId,
      channelName: channel.channelName,
      channelLabel: channel.channelLabel,
      channelArch: channel.channelArch,
      channelOrg: channel.channelOrg,
      parentChannelLabel: parentLabel || channel.parentChannelLabel || null,
      originalChannelLabel: channel.originalChannelLabel || null,
      childrenLabels: [],
      synced: channel.synced || false,
    };

    flatChannels.push(flatChannel);
    channelMap.set(channel.channelLabel, flatChannel);

    // Process children
    if (channel.children && channel.children.length > 0) {
      channel.children.forEach((child) => {
        processChannel(child, channel.channelLabel);
      });
    }
  };

  // Process all channels
  hierarchicalChannels.forEach((channel) => {
    processChannel(channel);
  });

  // Second pass: populate childrenIds
  flatChannels.forEach((channel) => {
    // Find all channels that have this channel as parent
    channel.childrenLabels = flatChannels
      .filter((ch) => ch.parentChannelLabel === channel.channelLabel)
      .map((ch) => ch.channelLabel);
  });

  return flatChannels;
}

/**
 * Gets root channels from a flat channel list
 */
export function getRootChannels(flatChannels: FlatChannel[]): FlatChannel[] {
  return flatChannels.filter((channel) => !channel.parentChannelLabel);
}

/**
 * Gets child channels for a specific parent channel
 */
export function getChildChannels(parentChannel: FlatChannel, flatChannels: FlatChannel[]): FlatChannel[] {
  return flatChannels.filter((channel) => channel.parentChannelLabel === parentChannel.channelLabel);
}

/**
 * Creates a map for quick channel lookups by label
 */
export function createChannelMap(flatChannels: FlatChannel[]): Map<string, FlatChannel> {
  const channelMap = new Map<string, FlatChannel>();
  flatChannels.forEach((channel) => {
    channelMap.set(channel.channelLabel, channel);
  });
  return channelMap;
}
