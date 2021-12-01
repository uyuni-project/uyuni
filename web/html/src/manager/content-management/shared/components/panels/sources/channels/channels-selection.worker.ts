// NB! Workers must import polyfills
import "manager/polyfills.ts";

import produce from "utils/produce";

// If we want to use more workers in the future, using a dedicated library such as Comlink or something similar might make sense
import WorkerMessages from "./channels-selection-messages";
import { DerivedChannel, DerivedBaseChannel, DerivedChildChannel } from "core/channels/type/channels.type";
import { channelsFiltersAvailable, FiltersType } from "./channels-selection.state";
import { derivedChannelsToRowDefinitions, rawChannelsToDerivedChannels } from "./channels-selection-transforms";

// eslint-disable-next-line no-restricted-globals
const context: Worker = self as any;

// TODO: Would be nice to break this off into a separate file
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
  const rows = derivedChannelsToRowDefinitions(baseChannels, state);
  context.postMessage({
    type: WorkerMessages.ROWS_CHANGED,
    rows,
    selectedChannelsCount: state.selectedChannelIds.size,
  });
}
