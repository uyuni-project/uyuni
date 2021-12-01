import { DerivedChannel } from "core/channels/type/channels.type";

export type FilterType = {
  id: string;
  text: string;
  isVisible: (arg0: DerivedChannel) => boolean;
  selectedByDefault: boolean;
};
export type FiltersType = {
  [key: string]: FilterType;
};

export const channelsFiltersAvailable: FiltersType = {
  vendors: {
    id: "vendors",
    text: "Vendors",
    isVisible: (c: DerivedChannel) => !c.custom,
    selectedByDefault: true,
  },
  custom: {
    id: "custom",
    text: "Custom",
    isVisible: (c: DerivedChannel) => c.custom && !c.isCloned,
    selectedByDefault: true,
  },
  clones: {
    id: "clones",
    text: "Clones",
    isVisible: (c: DerivedChannel) => c.isCloned,
    selectedByDefault: false,
  },
};

export const channelsFiltersAvailableValues = Object.values(channelsFiltersAvailable);

export const getInitialFiltersState = () =>
  channelsFiltersAvailableValues.filter((filter) => filter.selectedByDefault).map((filter) => filter.id) || [];

export type StateChannelsSelectionType = {
  activeFilters: Array<string>;
  selectedBaseChannelId: number;
  selectedChannelsIds: Array<number>;
  openGroupsIds: Array<number>;
  search: string;
};

export type ActionChannelsSelectionType =
  | { type: "search"; search: string }
  | { type: "toggle_filter"; filter: string }
  | { type: "toggle_channel"; channelId: number; baseId: number }
  | { type: "set_recommended"; enable: boolean; baseId: number }
  | { type: "open_group"; open: boolean; baseId: number }
  | { type: "lead_channel"; newBaseId: number };

export const initialStateChannelsSelection = (initialSelectedIds: Array<number>) => ({
  activeFilters: getInitialFiltersState(),
  selectedBaseChannelId: initialSelectedIds[0],
  selectedChannelsIds: initialSelectedIds,
  openGroupsIds: initialSelectedIds,
  search: "",
});
