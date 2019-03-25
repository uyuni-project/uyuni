// @flow
import type {ChannelsTreeType} from "core/channels/api/use-channels-tree-api";
import type {ChannelType} from "core/channels/type/channels.type";

import _intersection from "lodash/intersection";
import _isNil from "lodash/isNil";

export function getSelectedChannelsIdsInGroup (selectedChannelsIds: Array<number>, baseChannel: ChannelType): Array<number> {
  return _intersection(
    selectedChannelsIds,
    [baseChannel.id, ...baseChannel.children]
  );
}

export function getAllRecommentedIdsByBaseId (
  baseId: ?number,
  channelsTree: ChannelsTreeType,
  selectedChannelsIds: Array<number>
): {
  recommendedIds: Array<number>,
  recommendedIdsSelected: Array<number>,
  areRecommendedChildrenSelected: boolean,
} {

  if (_isNil(baseId)) {
    return {
      recommendedIds: [],
      recommendedIdsSelected: [],
      areRecommendedChildrenSelected: false,
    }
  };

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

