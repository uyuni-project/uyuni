// @flow

import {ChannelType} from "core/type/channels/channels.type"

import _xor from "lodash/xor";
import type {ChannelsTreeType} from "./api/use-channels";
import _difference from "lodash/difference";
import _union from "lodash/union";
import {getAllRecommentedIdsByBaseId} from "./channels-selection.utils";


export type FilterType = {id: string, text: string, isVisible: (ChannelType) => Boolean}
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
  selectedBaseChannelId: string,
  selectedChannelsIds: Array<string>,
  openGroupsIds: Array<string>,
  search: string,
}

export type ActionChannelsSelectionType =
  | {type: "search", search: string}
  | {type: "toggle_filter", filter: string}
  | {type: "toggle_channels", channelsIds: Array<string>, baseId: string}
  | {type: "set_recommended", enable: boolean, baseId: string}
  | {type: "open_group",  open: boolean, baseId: string}
  | {type: "lead_channel", newBaseId: string};


export const initialStateChannelsSelection = ({initialSelectedIds}: {initialSelectedIds: Array<string>}) => ({
  activeFilters: getInitialFiltersState(),
  selectedBaseChannelId: initialSelectedIds[0],
  selectedChannelsIds: initialSelectedIds,
  openGroupsIds: initialSelectedIds,
  search: ""
});

export const reducerChannelsSelection = (
  draftState: StateChannelsSelectionType,
  action: ActionChannelsSelectionType,
  channelsTree: ChannelsTreeType
) => {
  switch (action.type) {
    case 'search': {
      draftState.search = action.search;
      if(action.search) {
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
        draftState.openGroupsIds = openGroupsIds ;
      } else {
        draftState.openGroupsIds = [] ;
      }
      return draftState;
    }
    case "toggle_filter": {
      draftState.activeFilters = _xor(draftState.activeFilters, [action.filter]);
      return draftState;
    }
    case "toggle_channels": {
      const shouldEnableRecomended =
        action.channelsIds.length === 1
        && action.channelsIds[0] === action.baseId
        && !draftState.selectedChannelsIds.includes(action.channelsIds[0]);

      if(shouldEnableRecomended) {
        const {recommendedIds} = getAllRecommentedIdsByBaseId(action.baseId, channelsTree, draftState.selectedChannelsIds);
        draftState.selectedChannelsIds = _union(draftState.selectedChannelsIds, recommendedIds);
      } else {
        draftState.selectedChannelsIds = _xor(draftState.selectedChannelsIds, action.channelsIds);
      }
      return draftState;
    }
    case 'lead_channel': {
      const {recommendedIds} = getAllRecommentedIdsByBaseId(action.newBaseId, channelsTree, draftState.selectedChannelsIds);
      draftState.search = "";
      draftState.activeFilters = getInitialFiltersState();
      draftState.selectedBaseChannelId = action.newBaseId;
      draftState.selectedChannelsIds = _union(draftState.selectedChannelsIds, recommendedIds);
      draftState.openGroupsIds = draftState.selectedChannelsIds ;
      return draftState;
    }
    case 'set_recommended': {
      const {recommendedIds} = getAllRecommentedIdsByBaseId(
        action.baseId,
        channelsTree,
        draftState.selectedChannelsIds
      );

      if (action.enable) {
        draftState.selectedChannelsIds = _union(draftState.selectedChannelsIds, recommendedIds)
      } else {
        const recommendedWithoutLeadChannel =  recommendedIds.filter(id => draftState.selectedBaseChannelId !== id);
        draftState.selectedChannelsIds = _difference(draftState.selectedChannelsIds, recommendedWithoutLeadChannel);
      }
      return draftState;
    }
    case 'open_group': {
      if(action.open) {
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
};
