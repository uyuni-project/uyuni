import { DerivedBaseChannel, DerivedChannel, RawChannelType } from "core/channels/type/channels.type";
import { RowDefinition, RowType } from "./channels-selection-rows";
import State from "./channels-selection-state";

// TODO: Add tests
export function rawChannelsToDerivedChannels(
  rawChannels: RawChannelType[],
  rawRequiresMap: { [key: string]: number[] | undefined }
) {
  // Keep track of all channels we store
  const channelsMap = new Map<number, DerivedChannel>();
  // TODO: Type all of this
  // NB! The data we receive here is already a copy since we're in a worker so it's safe to modify it directly
  const baseChannels = rawChannels.map((rawChannel: RawChannelType) => {
    // TODO: This cast is not correct
    // If we want to reduce copy overhead we could only pick the fields we need here
    const baseChannel: DerivedBaseChannel = rawChannel.base as DerivedBaseChannel;

    // Precompute filtering values so we only do this once
    baseChannel.standardizedName = baseChannel.name.toLocaleLowerCase();

    baseChannel.recommendedChildrenIds = new Set<number>();
    baseChannel.children = rawChannel.children.map((child) => {
      const derivedChild = {
        ...child,
        parent: baseChannel,
        standardizedName: child.name.toLocaleLowerCase(),
      };
      channelsMap.set(child.id, derivedChild);
      if (child.recommended) {
        baseChannel.recommendedChildrenIds.add(child.id);
      }
      return derivedChild;
    });

    channelsMap.set(baseChannel.id, baseChannel);

    return baseChannel;
  });

  // Create a two-way mapping of what channels require what channels, and what channels are required by what channels
  const requiresMap = new Map<number, Set<DerivedChannel> | undefined>();
  const requiredByMap = new Map<number, Set<DerivedChannel> | undefined>();
  for (const channelIdString in rawRequiresMap) {
    const channelId = parseInt(channelIdString, 10);
    const channel = channelsMap.get(channelId);
    if (isNaN(channelId) || !channel) {
      throw new RangeError("Invalid channel id");
    }

    const requiredChannels = rawRequiresMap[channelIdString]
      ?.map((id) => channelsMap.get(id) as DerivedChannel) // We know these values exist
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

  return { baseChannels, channelsMap, requiresMap, requiredByMap };
}

const getTooltipData = (channelId: number, state: State) => {
  const [requiresNames, requiredByNames] = [state.requiresMap.get(channelId), state.requiredByMap.get(channelId)].map(
    (maybeSet) =>
      Array.from(maybeSet || [])
        .filter(Boolean)
        .map((channel) => channel.name)
  );
  return {
    requiresNames,
    requiredByNames,
  };
};

// TODO: Add tests
export function derivedChannelsToRowDefinitions(derivedChannels: DerivedBaseChannel[], state: State): RowDefinition[] {
  // TODO: Here and elsewhere, this reduce can become just a regular for loop if we want to go faster
  return derivedChannels.reduce((result, channel) => {
    // TODO: Either is open or matches search
    const isOpen = state.isOpen(channel.id);
    const isSelected = state.isSelected(channel.id);

    // We need to figure out what state the children are in before we can store the parent state
    let children: RowDefinition[] = [];
    let selectedChildrenCount = 0;
    let recommendedChildrenCount = 0;
    let selectedRecommendedChildrenCount = 0;
    const parentRequires = state.requiresMap.get(channel.id);
    if (channel.children.length) {
      channel.children.forEach((child) => {
        const isChildSelected = state.isSelected(child.id);
        selectedChildrenCount += Number(isChildSelected);
        const isChildRecommended = child.recommended;
        recommendedChildrenCount += Number(isChildRecommended);
        selectedRecommendedChildrenCount += Number(isChildSelected && isChildRecommended);

        if (isOpen) {
          // const tooltip
          children.push({
            type: RowType.Child,
            id: child.id,
            channelName: child.name,
            isSelected: isChildSelected,
            isRequired: Boolean(parentRequires?.has(child)),
            isRecommended: child.recommended,
            tooltipData: getTooltipData(child.id, state),
          });
        }
      });
    } else if (isOpen) {
      children.push({
        type: RowType.EmptyChild,
        id: `empty_child_${channel.id}`,
      });
    }

    result.push({
      type: RowType.Parent,
      id: channel.id,
      channelName: channel.name,
      isOpen,
      isSelected,
      isSelectedBaseChannel: channel.id === state.selectedBaseChannelId,
      selectedChildrenCount,
    });
    if (isOpen && recommendedChildrenCount) {
      result.push({
        type: RowType.RecommendedToggle,
        id: `recommended_toggle_${channel.id}`,
        channelId: channel.id,
        areAllRecommendedChildrenSelected: recommendedChildrenCount === selectedRecommendedChildrenCount,
      });
    }
    result.push(...children);

    return result;
  }, [] as RowDefinition[]);
}
