import { BaseChannelType } from "core/channels/type/channels.type";

export type FilterType = {
  id: string;
  text: string;
  isVisible: (arg0: BaseChannelType) => boolean;
  selectedByDefault: boolean;
};
export type FiltersType = {
  [key: string]: FilterType;
};

export const channelsFiltersAvailable: FiltersType = {
  vendors: {
    id: "vendors",
    text: "Vendors",
    isVisible: (c: BaseChannelType) => !c.custom,
    selectedByDefault: true,
  },
  custom: {
    id: "custom",
    text: "Custom",
    isVisible: (c: BaseChannelType) => c.custom && !c.isCloned,
    selectedByDefault: true,
  },
  clones: {
    id: "clones",
    text: "Clones",
    isVisible: (c: BaseChannelType) => c.isCloned,
    selectedByDefault: false,
  },
};

export const channelsFiltersAvailableValues = Object.values(channelsFiltersAvailable);

export const getInitialFiltersState = () =>
  channelsFiltersAvailableValues.filter((filter) => filter.selectedByDefault).map((filter) => filter.id) || [];
