// @flow

import type {ChannelsTreeType} from "./api/use-channels";
import {channelsFiltersAvailable} from "./channels-selection.state";
import type {ChannelType} from "core/type/channels/channels.type";
import _intersection from "lodash/intersection";

export function orderBaseChannels (channelsTree: ChannelsTreeType, selectedBaseChannelId: string): Array<ChannelType> {
  const orderedBaseChannels = channelsTree.baseIds
    .map(cId => channelsTree.channelsById[cId])
    .sort((b1, b2) => b1.id - b2.id)
    .filter(b => b.id !== selectedBaseChannelId);

  if(selectedBaseChannelId) {
    orderedBaseChannels.unshift(channelsTree.channelsById[selectedBaseChannelId])
  }

  return orderedBaseChannels;
}

export function getVisibleChannels (channelsTree: ChannelsTreeType, activeFilters: Array<string>): Array<string> {
  return Object.values(channelsTree.channelsById)
    .filter( c =>
      activeFilters
        .map(filterId => channelsFiltersAvailable[filterId])
        .some(filter => filter.isVisible(c))
    )
    .map((c: ChannelType) => c.id);
}

export function getSelectedChannelsIdsInGroup (selectedChannelsIds: Array<string>, baseChannel: ChannelType): Array<string> {
  return _intersection(
    selectedChannelsIds,
    [baseChannel.id, ...baseChannel.children]
  );
}

export function isGroupVisible (
  baseChannel: ChannelType,
  channelsTree: ChannelsTreeType,
  visibleChannels: Array<ChannelType>,
  selectedChannelsIdsInGroup: Array<string>,
  selectedBaseChannelId: string,
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

export function getAllRecommentedIdsByBaseId (
  baseId: string,
  channelsTree: ChannelsTreeType,
  selectedChannelsIds: Array<string>
): {
  recommendedIds: Array<string>,
  recommendedIdsSelected: Array<string>,
  areRecommendedChildrenSelected: boolean,
} {
  const recommendedChildrenIds = channelsTree.channelsById[baseId]
    .children
    .map(cId => channelsTree.channelsById[cId])
    .filter(c => c.recommended)
    .map(c => c.id);
  const recommendedIds = [baseId, ...recommendedChildrenIds];
  const recommendedIdsSelected = _intersection(recommendedIds, selectedChannelsIds);
  const areRecommendedChildrenSelected = recommendedIds.length === recommendedIdsSelected.length;

  return {
    recommendedIds,
    recommendedIdsSelected,
    areRecommendedChildrenSelected
  }
}
