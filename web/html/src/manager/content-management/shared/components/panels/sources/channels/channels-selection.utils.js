// @flow
import type {ChannelsTreeType} from "core/channels/api/use-channels-tree-api";
import {getChannelsTreeValues} from "core/channels/api/use-channels-tree-api";
import type {ChannelType} from "core/channels/type/channels.type";
import {channelsFiltersAvailable} from "./channels-selection.state";

export function orderBaseChannels (channelsTree: ChannelsTreeType, selectedBaseChannelId: number): Array<ChannelType> {
  const orderedBaseChannels = channelsTree.baseIds
    .map(cId => channelsTree.channelsById[cId])
    .sort((b1, b2) => b1.id - b2.id)
    .filter(b => b.id !== selectedBaseChannelId);

  if(selectedBaseChannelId) {
    orderedBaseChannels.unshift(channelsTree.channelsById[selectedBaseChannelId])
  }

  return orderedBaseChannels;
}

export function getVisibleChannels (channelsTree: ChannelsTreeType, activeFilters: Array<string>): Array<number> {
  return getChannelsTreeValues(channelsTree)
    .filter( c =>
      activeFilters
        .map(filterId => channelsFiltersAvailable[filterId])
        .some(filter => filter.isVisible(c))
    )
    .map((c: ChannelType) => c.id);
}

export function isGroupVisible (
  baseChannel: ChannelType,
  channelsTree: ChannelsTreeType,
  visibleChannels: Array<number>,
  selectedChannelsIdsInGroup: Array<number>,
  selectedBaseChannelId: number,
  search:string
) {
  const isSearchPresentInGroup = baseChannel.name.toLowerCase().includes(search.toLowerCase()) ||
    baseChannel.children.some(cId => channelsTree.channelsById[cId].name.toLowerCase().includes(search.toLowerCase()));
  const isSameArchAsSelectedBase = baseChannel.archLabel === channelsTree.channelsById[selectedBaseChannelId].archLabel;
  const hasAtLeastOneSelection = selectedChannelsIdsInGroup.length > 0;

  // We want to show the group either it's compatible with the selected channel or has at least one selection
  const isGroupVisible = (
      isSameArchAsSelectedBase
      || hasAtLeastOneSelection
    )
    && (
      visibleChannels.includes(baseChannel.id)
      && isSearchPresentInGroup
    )


  return isGroupVisible;
}
