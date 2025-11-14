import { ChannelType } from "core/channels/type/channels.type";

export const toCanonicalRequires = (
  rawRequiresMap: Record<string, number[] | undefined>,
  channelsMap: Map<number, ChannelType>
) => {
  // Create a two-way mapping of what channels require what channels, and what channels are required by what channels
  const requiresMap = new Map<number, Set<ChannelType> | undefined>();
  const requiredByMap = new Map<number, Set<ChannelType> | undefined>();
  for (const channelIdString in rawRequiresMap) {
    const channelId = parseInt(channelIdString, 10);
    const channel = channelsMap.get(channelId);
    if (isNaN(channelId) || !channel) {
      throw new RangeError("Invalid channel id");
    }

    const requiredChannels = rawRequiresMap[channelIdString]
      ?.filter((requiredChannelId) => {
        // The original data includes the channel's own id, don't include it in the set
        return requiredChannelId && requiredChannelId !== channel.id && channelsMap.has(requiredChannelId);
      })
      ?.map((requiredChannelId) => channelsMap.get(requiredChannelId)!);

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
