// @flow
import type {ChannelsTreeType} from "core/channels/api/use-channels-tree-api";
import _xor from "lodash/xor";
import _difference from "lodash/difference";
import _union from "lodash/union";
import type {RequiredChannelsResultType} from "core/channels/api/use-mandatory-channels-api";
import type {ChannelType} from "core/channels/type/channels.type";
import {getAllRecommentedIdsByBaseId} from "core/channels/utils/channels-state.utils";
import {getChannelsToToggleWithDependencies} from "core/channels/utils/channels-dependencies.utils";


export type FilterType = {id: string, text: string, isVisible: (ChannelType) => boolean}
export type FiltersType = { [key: string]: FilterType }

export const channelsFiltersAvailable: FiltersType = {
  vendors: {
    id: 'vendors',
    text: 'Vendors',
    isVisible: (c: ChannelType) => !c.custom,
  },
  custom: {
    id: 'custom',
    text: 'Custom',
    isVisible: (c: ChannelType) => c.custom && !c.isCloned,
  },
  clones: {
    id: 'clones',
    text: 'Clones',
    isVisible: (c: ChannelType) => c.isCloned,
  }
}

export const getChannelsFiltersAvailableValues = (): Array<FilterType> => (Object.values(channelsFiltersAvailable) : any);

export const getInitialFiltersState = (): Array<string> => Object.keys(channelsFiltersAvailable);


export type StateChannelsSelectionType = {
  activeFilters: Array<string>,
  selectedBaseChannelId: number,
  selectedChannelsIds: Array<number>,
  openGroupsIds: Array<number>,
  search: string,
}

export type ActionChannelsSelectionType =
  | {type: "search", search: string}
  | {type: "toggle_filter", filter: string}
  | {type: "toggle_channel", channelId: number, baseId: number}
  | {type: "set_recommended", enable: boolean, baseId: number}
  | {type: "open_group",  open: boolean, baseId: number}
  | {type: "lead_channel", newBaseId: number};


export const initialStateChannelsSelection = ({initialSelectedIds}: {initialSelectedIds: Array<number>}) => ({
  activeFilters: getInitialFiltersState(),
  selectedBaseChannelId: initialSelectedIds[0],
  selectedChannelsIds: initialSelectedIds,
  openGroupsIds: initialSelectedIds,
  search: ""
});

export const reducerChannelsSelection = (
  draftState: StateChannelsSelectionType,
  action: ActionChannelsSelectionType,
  channelsTree: ChannelsTreeType,
  requiredChannelsResult: RequiredChannelsResultType,
) => {
  switch (action.type) {
    case 'search': {
      draftState.search = action.search;
      if (action.search) {
        const search = action.search;
        // If the search term is present in the group it will open it
        const openGroupsIds =
          channelsTree.baseIds
            .map(cId => channelsTree.channelsById[cId])
            .filter(base =>
              base.children
                .map(cId => channelsTree.channelsById[cId])
                .some(c => c.name.toLowerCase().includes(search.toLowerCase()))
              || base.name.toLowerCase().includes(search.toLowerCase())
            )
            .map(c => c.id);
        draftState.openGroupsIds = openGroupsIds;
      } else {
        draftState.openGroupsIds = [];
      }
      return draftState;
    }
    case "toggle_filter": {
      draftState.activeFilters = _xor(draftState.activeFilters, [action.filter]);
      return draftState;
    }
    case "toggle_channel": {
      const channelId = action.channelId;
      const isSelection = !draftState.selectedChannelsIds.includes(channelId);
      const isBaseChannel = channelId === action.baseId;
      const shouldEnableRecomended = isBaseChannel && isSelection;

      let channelsToToggle = [channelId];
      if (shouldEnableRecomended) {
        const {recommendedIds} = getAllRecommentedIdsByBaseId(action.baseId, channelsTree, draftState.selectedChannelsIds);
        channelsToToggle = _union(channelsToToggle, recommendedIds);
      }
      channelsToToggle = getChannelsToToggleWithDependencies(channelsToToggle, requiredChannelsResult, isSelection);

      if (isSelection) {
        if(isBaseChannel) {
          draftState.openGroupsIds.push(channelId);
        }
        draftState.selectedChannelsIds = _union(draftState.selectedChannelsIds, channelsToToggle);
      } else {
        const channelsToToggleWithoutLeadChannel = channelsToToggle.filter(id => draftState.selectedBaseChannelId !== id);
        draftState.selectedChannelsIds = _difference(draftState.selectedChannelsIds, channelsToToggleWithoutLeadChannel);
      }
      return draftState;
    }
    case 'lead_channel': {
      const newBaseId = action.newBaseId;
      const newBaseArchLabel = channelsTree.channelsById[newBaseId].archLabel

      let channelsToToggle = [newBaseId];
      const {recommendedIds} = getAllRecommentedIdsByBaseId(newBaseId, channelsTree, draftState.selectedChannelsIds);
      channelsToToggle = _union(channelsToToggle, recommendedIds);
      channelsToToggle = getChannelsToToggleWithDependencies(channelsToToggle, requiredChannelsResult, true);

      // Unselect all the incompatible selected channels for the new lead Channel
      const selectedChannelsWithIncompatible = draftState.selectedChannelsIds
        .map(cId => channelsTree.channelsById[cId])
        .filter(c => c.archLabel !== newBaseArchLabel)
        .map(c => c.id);
      draftState.selectedChannelsIds = _difference(draftState.selectedChannelsIds, selectedChannelsWithIncompatible);

      draftState.selectedChannelsIds = _union(draftState.selectedChannelsIds, channelsToToggle);
      draftState.search = "";
      draftState.activeFilters = getInitialFiltersState();
      draftState.selectedBaseChannelId = newBaseId;
      draftState.openGroupsIds = draftState.selectedChannelsIds;
      return draftState;
    }
    case 'set_recommended': {
      let channelsToToggle = [];
      const {recommendedIds} = getAllRecommentedIdsByBaseId(action.baseId, channelsTree, draftState.selectedChannelsIds);
      channelsToToggle = _union(channelsToToggle, recommendedIds);
      channelsToToggle = getChannelsToToggleWithDependencies(channelsToToggle, requiredChannelsResult, action.enable);

      if (action.enable) {
        draftState.selectedChannelsIds = _union(draftState.selectedChannelsIds, channelsToToggle)
      } else {
        const channelsToToggleWithoutLeadChannel = channelsToToggle.filter(id => draftState.selectedBaseChannelId !== id);
        draftState.selectedChannelsIds = _difference(draftState.selectedChannelsIds, channelsToToggleWithoutLeadChannel);
      }
      return draftState;
    }
    case 'open_group': {
      if (action.open) {
        draftState.openGroupsIds.push(action.baseId);
      } else {
        const baseId = action.baseId;
        draftState.openGroupsIds = draftState.openGroupsIds.filter(
          openId => !(openId === baseId || channelsTree.channelsById[baseId].children.includes(openId))
        );
      }
      return draftState;
    }
    default:
      throw new Error();
  }
}
