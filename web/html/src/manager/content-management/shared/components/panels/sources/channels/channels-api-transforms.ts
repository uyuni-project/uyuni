import { BaseChannelType, ChildChannelType } from "core/channels/type/channels.type";

export const canonicalizeBase = (channel: BaseChannelType): void => {
  // Precompute a name value for filtering so we only do this once
  channel.standardizedName = channel.name.toLocaleLowerCase();
  channel.recommendedChildren = [];
};

export const canonicalizeChild = (channel: ChildChannelType, parentChannel: BaseChannelType): void => {
  channel.standardizedName = channel.name.toLocaleLowerCase();
  channel.parent = parentChannel;
};

export const toCanonicalRequires = (
  rawRequiresMap: { [key: string]: number[] | undefined },
  channelsMap: Map<number, BaseChannelType | ChildChannelType>
) => {
  // Create a two-way mapping of what channels require what channels, and what channels are required by what channels
  const requiresMap = new Map<number, Set<BaseChannelType | ChildChannelType> | undefined>();
  const requiredByMap = new Map<number, Set<BaseChannelType | ChildChannelType> | undefined>();
  for (const channelIdString in rawRequiresMap) {
    const channelId = parseInt(channelIdString, 10);
    const channel = channelsMap.get(channelId);
    if (isNaN(channelId) || !channel) {
      throw new RangeError("Invalid channel id");
    }

    const requiredChannels = rawRequiresMap[channelIdString]
      ?.map((id) => channelsMap.get(id) as BaseChannelType | ChildChannelType) // We know these values exist
      .filter((requiredChannel) => requiredChannel.id !== channel.id); // The original data includes the channel's own id, don't include it in the set
    if (requiredChannels?.length) {
      requiresMap.set(channelId, new Set(requiredChannels));
      requiredChannels.forEach((requiredChannel) => {
        if (requiredByMap.has(requiredChannel.id)) {
          requiredByMap.get(requiredChannel.id)?.add(channel);
        } else {
          requiredByMap.set(requiredChannel.id, new Set([channel]));
        }
      });
    }
  }
  return { requiresMap, requiredByMap };
};
