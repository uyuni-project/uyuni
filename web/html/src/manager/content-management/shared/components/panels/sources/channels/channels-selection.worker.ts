// NB! Workers must import polyfills
import "manager/polyfills.ts";

import produce from "utils/produce";

// If we want to use more workers in the future, using a dedicated library such as Comlink or something similar might make sense
import WorkerMessages from "./channels-selection-messages";
import {
  DerivedChannel,
  DerivedBaseChannel,
  DerivedChildChannel,
  RawChannelType,
} from "core/channels/type/channels.type";
import { channelsFiltersAvailable, FiltersType } from "./channels-selection.state";
import { RowType, RowDefinition } from "./channels-selection-rows";

// eslint-disable-next-line no-restricted-globals
const context: Worker = self as any;

const state = {
  /** An array of all base channels after they've passed initial processing */
  baseChannels: undefined as DerivedBaseChannel[] | undefined,
  /** A map from a channel id to any known channel */
  channelsMap: new Map<number, DerivedChannel>(),
  /** A map from a channel id to a set of channel ids this channel requires */
  requiresMap: new Map<number, Set<number> | undefined>(),
  /** A map from a channel id to a set of channels that require this channel */
  requiredByMap: new Map<number, Set<number> | undefined>(),
  /** Set of currently open base channel ids */
  openBaseChannelIds: new Set<number>(),
  /** Set of currently selected channel ids */
  selectedChannelIds: new Set<number>(),
  /** Currently selected base channel id */
  selectedBaseChannelId: undefined as number | undefined,
  /** User search string */
  search: "",
  /** User-selected filters such as vendors, custom, clones */
  activeFilters: [] as (keyof FiltersType)[],
};

const isBase = (input: DerivedChannel | undefined): input is DerivedBaseChannel => {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "children"));
};

const isChild = (input: DerivedChannel | undefined): input is DerivedChildChannel => {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "parent"));
};

const selectRecursively = (channelId: number) => {
  if (state.selectedChannelIds.has(channelId)) {
    return;
  }
  state.selectedChannelIds.add(channelId);

  const channel = state.channelsMap.get(channelId);
  if (isChild(channel)) {
    // If we selected a child, open the parent
    state.openBaseChannelIds.add(channel.parent.id);
  }

  // Also select any channels that this channel requires
  state.requiresMap.get(channelId)?.forEach((id) => selectRecursively(id));
};

const deselectRecursively = (channelId: number) => {
  if (!state.selectedChannelIds.has(channelId)) {
    return;
  }
  state.selectedChannelIds.delete(channelId);

  // If we deselected a parent, deselect all of its children
  const channel = state.channelsMap.get(channelId);
  if (isBase(channel)) {
    channel.children.forEach((child) => deselectRecursively(child.id));
  }

  // Also deselect any channels that require this channel
  state.requiredByMap.get(channelId)?.forEach((id) => deselectRecursively(id));
};

// Respond to message from parent thread
context.addEventListener("message", async ({ data }) => {
  switch (data.type) {
    // The worker can't currently integrate with our network layer due to its reliance on jQuery, in the future the worker could do the request itself
    case WorkerMessages.SET_CHANNELS: {
      if (!Array.isArray(data.channels) || !data.mandatoryChannelsMap) {
        throw new TypeError("Insufficient channel data");
      }
      const { baseChannels, channelsMap, requiresMap, requiredByMap } = rawChannelsToDerivedChannels(
        data.channels,
        data.mandatoryChannelsMap
      );
      onChange({ baseChannels, channelsMap, requiresMap, requiredByMap });
      return;
    }
    case WorkerMessages.SET_SEARCH: {
      const search = data.search;
      if (typeof search !== "string") {
        throw new TypeError("No search string");
      }
      onChange({ search });
      return;
    }
    case WorkerMessages.SET_SELECTED_BASE_CHANNEL_ID: {
      const selectedBaseChannelId = data.selectedBaseChannelId;
      if (typeof selectedBaseChannelId !== "number" || isNaN(selectedBaseChannelId)) {
        throw new TypeError("No base channel id");
      }
      state.openBaseChannelIds.add(selectedBaseChannelId);
      selectRecursively(selectedBaseChannelId);
      onChange({ selectedBaseChannelId });
      return;
    }
    case WorkerMessages.SET_ACTIVE_FILTERS: {
      const activeFilters = data.activeFilters;
      if (!Array.isArray(activeFilters)) {
        throw new TypeError("No valid active filters");
      }
      onChange({ activeFilters });
      return;
    }
    case WorkerMessages.TOGGLE_IS_CHANNEL_OPEN: {
      const channelId = data.channelId;
      if (typeof channelId === "undefined") {
        throw new TypeError("Channel not found");
      }
      if (state.openBaseChannelIds.has(channelId)) {
        state.openBaseChannelIds.delete(channelId);
      } else {
        state.openBaseChannelIds.add(channelId);
      }
      onChange();
      return;
    }
    case WorkerMessages.TOGGLE_IS_CHANNEL_SELECTED: {
      const channelId = data.channelId;
      const channel = state.channelsMap.get(channelId);
      if (typeof channel === "undefined") {
        throw new TypeError("Channel not found");
      }

      if (state.selectedChannelIds.has(channelId)) {
        deselectRecursively(channelId);
      } else {
        selectRecursively(channelId);
        // If we selected a parent on the first level, also select all recommended children
        const channel = state.channelsMap.get(channelId);
        if (isBase(channel)) {
          channel.children.forEach((child) => {
            if (child.recommended) {
              selectRecursively(child.id);
            }
          });
        }
      }
      onChange();
      return;
    }
    case WorkerMessages.SET_RECOMMENDED_CHILDREN_ARE_SELECTED: {
      const channelId = data.channelId;
      const channel = state.channelsMap.get(channelId);
      if (typeof channel === "undefined" || !isBase(channel)) {
        throw new TypeError("Channel is not a base channel or is not found");
      }

      if (data.selected) {
        channel.recommendedChildrenIds.forEach((id) => selectRecursively(id));
      } else {
        channel.recommendedChildrenIds.forEach((id) => deselectRecursively(id));
      }
      onChange();
      return;
    }
    default:
      throw new RangeError(`Unknown message type, got ${data.type}`);
  }
});

// Whenever we receive new inputs or data, compute and return an updated result
function onChange(stateChange: Partial<typeof state> = {}) {
  Object.assign(state, stateChange);

  // If we don't have channels or a selected base channel id yet, there's nothing to do
  if (typeof state.baseChannels === "undefined" || typeof state.selectedBaseChannelId === "undefined") {
    return;
  }

  /**
   * If channels changed or the chosen base channel has changed, ensure channels are properly sorted
   * We store the sorting and only do this on changes so we don't resort for other basic operations
   */
  if (typeof stateChange.baseChannels !== "undefined" || typeof stateChange.selectedBaseChannelId !== "undefined") {
    state.baseChannels = state.baseChannels.sort((a, b) => {
      // If a base has been selected, sort it to the beginning...
      if (state.selectedBaseChannelId) {
        if (a.id === state.selectedBaseChannelId) return -1;
        if (b.id === state.selectedBaseChannelId) return +1;
      }
      // ...otherwise sort by id
      return a.id - b.id;
    });
  }

  /**
   * This object wrap-unwrap is only required so we can filter and modify the draft in immer at the same time.
   * Immer doesn't allow you to return a new draft value and modify the draft at the same time since it's usually a bug.
   */
  const { baseChannels } = produce({ baseChannels: state.baseChannels }, (draft): void => {
    // Filter by categories and search strings first if possible so everything else is cheaper
    if (state.activeFilters.length) {
      const filters = state.activeFilters.map((name) => channelsFiltersAvailable[name].isVisible);
      draft.baseChannels = draft.baseChannels.filter((channel) => {
        return filters.some((filter) => filter(channel));
      });
    }

    if (state.search) {
      const search = state.search.toLocaleLowerCase();
      draft.baseChannels = draft.baseChannels.filter((channel) => {
        // NB! We filter the children as a side-effect here, sorry
        channel.children = channel.children.filter((child) => child.standardizedName.includes(search));
        // If the base channel name matches search or we have any children left after the above, include the base channel
        const matchesSearch = channel.standardizedName.includes(search) || channel.children.length > 0;

        // If the search _changed_ then open base channels that match, otherwise leave user's selection be
        if (typeof stateChange.search !== "undefined") {
          if (matchesSearch) {
            state.openBaseChannelIds.add(channel.id);
          } else {
            state.openBaseChannelIds.delete(channel.id);
          }
        }

        return matchesSearch;
      });
    } else if (typeof stateChange.search !== "undefined") {
      // If the search was cleared, close all base channels
      state.openBaseChannelIds.clear();
    }
  });

  // Convert whatever we have remaining after all the filters etc into renderable row definitions
  const rows = derivedChannelsToRowDefinitions(baseChannels, state.selectedBaseChannelId);
  context.postMessage({
    type: WorkerMessages.ROWS_CHANGED,
    rows,
    selectedChannelsCount: state.selectedChannelIds.size,
  });
}

// TODO: Export and add tests to this
function rawChannelsToDerivedChannels(
  rawChannels: RawChannelType[],
  rawRequiresMap: { [key: string]: number[] | undefined }
) {
  // Create a two-way mapping of what channels require what channels, and what channels are required by what channels
  const requiresMap = new Map<number, Set<number> | undefined>();
  const requiredByMap = new Map<number, Set<number> | undefined>();
  for (const channelIdString in rawRequiresMap) {
    const channelId = parseInt(channelIdString, 10);
    if (isNaN(channelId)) {
      throw new RangeError("Invalid channel id");
    }
    const requiredChannelIds = rawRequiresMap[channelIdString];
    if (requiredChannelIds?.length) {
      requiresMap.set(channelId, new Set(requiredChannelIds));
      requiredChannelIds.forEach((requiredChannelId) => {
        if (requiredByMap.has(requiredChannelId)) {
          requiredByMap.get(requiredChannelId)?.add(channelId);
        } else {
          requiredByMap.set(requiredChannelId, new Set([channelId]));
        }
      });
    }
  }

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
  return { baseChannels, channelsMap, requiresMap, requiredByMap };
}

// TODO: Export and test this
function derivedChannelsToRowDefinitions(
  derivedChannels: DerivedBaseChannel[],
  selectedBaseChannelId: number
): RowDefinition[] {
  // TODO: Here and elsewhere, this reduce can become just a regular for loop if we want to go faster
  return derivedChannels.reduce((result, channel) => {
    const isOpen = state.openBaseChannelIds.has(channel.id);
    const isSelected = state.selectedChannelIds.has(channel.id);

    // We need to figure out what state the children are in before we can store the parent state
    let children: RowDefinition[] = [];
    let selectedChildrenCount = 0;
    let recommendedChildrenCount = 0;
    let selectedRecommendedChildrenCount = 0;
    const parentRequires = state.requiresMap.get(channel.id);
    if (channel.children.length) {
      channel.children.forEach((child) => {
        const isChildSelected = state.selectedChannelIds.has(child.id);
        selectedChildrenCount += Number(isChildSelected);
        const isChildRecommended = child.recommended;
        recommendedChildrenCount += Number(isChildRecommended);
        selectedRecommendedChildrenCount += Number(isChildSelected && isChildRecommended);

        if (isOpen) {
          children.push({
            type: RowType.Child,
            id: child.id,
            channelName: channel.name,
            isSelected: isChildSelected,
            isRequired: Boolean(parentRequires?.has(child.id)),
            isRecommended: child.recommended,
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
      isSelectedBaseChannel: channel.id === selectedBaseChannelId,
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
