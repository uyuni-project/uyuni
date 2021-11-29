// TODO: Does all of this work with HMR?

// NB! Workers must import polyfills
// TODO: Find a pattern to ensure this always happens
import "manager/polyfills.ts";

import produce from "utils/produce";

// If we want to use more workers in the future, using a dedicated library such as Comlink or something similar might make sense
import WorkerMessages from "./channels-selection-messages";
import { DerivedBaseChannel, RawChannelType } from "core/channels/type/channels.type";
import { channelsFiltersAvailable } from "./channels-selection.state";
import { RowType, RowDefinition } from "./channels-selection-rows";

// eslint-disable-next-line no-restricted-globals
const context: Worker = self as any;

const state = {
  baseChannels: undefined as DerivedBaseChannel[] | undefined,
  baseChannelsMap: {} as { [key: number]: DerivedBaseChannel | undefined },
  openBaseChannelIds: new Set<number>(),
  selectedBaseChannelId: undefined as number | undefined,
  search: "",
  activeFilters: [] as string[],
};

// Respond to message from parent thread
context.addEventListener("message", async ({ data }) => {
  switch (data.type) {
    // The worker can't currently integrate with our network layer due to its reliance on jQuery, in the future the worker could do the request itself
    case WorkerMessages.SET_CHANNELS: {
      if (!Array.isArray(data.channels) || !data.mandatoryChannelsMap) {
        throw new TypeError("Insufficient channel data");
      }
      const { baseChannels, baseChannelsMap } = rawChannelsToDerivedChannels(data.channels, data.mandatoryChannelsMap);
      onChange({ baseChannels, baseChannelsMap });
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
    case WorkerMessages.TOGGLE_CHANNEL_IS_OPEN: {
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
  if (stateChange.baseChannels || stateChange.selectedBaseChannelId) {
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
    // TODO: See https://github.com/uyuni-project/uyuni/blob/c88fc74f9d6ff81d9fcfa32347d4b5d75a579dd5/web/html/src/manager/content-management/shared/components/panels/sources/channels/channels-selection.utils.ts#L19
    if (state.activeFilters.length) {
      const filters = state.activeFilters.map((name) => channelsFiltersAvailable[name].isVisible);
      draft.baseChannels = draft.baseChannels.filter((channel) => {
        // TODO: Do we need the parent-child logic here?
        // TODO: Fix types
        return filters.some((filter) => filter(channel as any));
      });
    }

    if (state.search) {
      const search = state.search.toLocaleLowerCase();
      draft.baseChannels = draft.baseChannels.filter((channel) => {
        // NB! We filter the children as a side-effect here, sorry
        channel.children = channel.children.filter((child) => child.standardizedName.includes(search));
        // If the base channel name matches search or we have any children left after the above, include the base channel
        const matchesSearch = channel.standardizedName.includes(search) || channel.children.length > 0;

        // TODO: If the search _changed_ then open groups that match, otherwise leave user's selection be
        // TODO: See how this matches other open-close logic and test
        if (stateChange.search) {
          if (matchesSearch) {
            state.openBaseChannelIds.add(channel.id);
          } else {
            state.openBaseChannelIds.delete(channel.id);
          }
          // channel.isOpen = matchesSearch;
        }

        return matchesSearch;
      });
    }
  });

  console.log(baseChannels);

  // Convert whatever we have remaining after all the filters etc into renderable row definitions
  const rows = derivedChannelsToRowDefinitions(baseChannels, state.selectedBaseChannelId);
  context.postMessage({
    type: WorkerMessages.ROWS_CHANGED,
    rows,
  });
}

// TODO: Export and add tests to this
function rawChannelsToDerivedChannels(
  rawChannels: RawChannelType[],
  mandatoryChannelsMap: Map<unknown, unknown[] | undefined>
) {
  const baseChannelsMap: Record<number, DerivedBaseChannel | undefined> = {};
  // TODO: Type all of this
  // NB! The data we receive here is already a copy since we're in a worker so it's safe to modify it directly
  const baseChannels = rawChannels.map((rawChannel: RawChannelType) => {
    // TODO: Make an object from scratch instead since rawChannels are read-only?
    // Or make separate state to track isOpen etc

    // TODO: This cast is not correct
    // If we want to reduce copy overhead we could only pick the fields we need here
    const baseChannel: DerivedBaseChannel = rawChannel.base as DerivedBaseChannel;
    // baseChannel.isOpen = false;

    // This should always be available, but just in case
    baseChannel.mandatory = mandatoryChannelsMap[baseChannel.id] || [];
    // Precompute filtering values so we only do this once
    baseChannel.standardizedName = baseChannel.name.toLocaleLowerCase();

    baseChannel.children = rawChannel.children.map((child) => ({
      ...child,
      parent: baseChannel,
      mandatory: mandatoryChannelsMap[child.id] || [],
      standardizedName: child.name.toLocaleLowerCase(),
    }));

    baseChannelsMap[baseChannel.id] = baseChannel;

    return baseChannel;
  });
  return { baseChannels, baseChannelsMap };
}

function derivedChannelsToRowDefinitions(
  derivedChannels: DerivedBaseChannel[],
  selectedBaseChannelId: number
): RowDefinition[] {
  // TODO: Here and elsewhere, this reduce can become just a regular for loop if we want to go faster
  // TODO: Implement
  return derivedChannels.reduce((result, channel) => {
    const isOpen = state.openBaseChannelIds.has(channel.id);
    result.push({
      type: RowType.Parent,
      id: channel.id,
      isOpen,
      isSelectedBaseChannel: channel.id === selectedBaseChannelId,
      channel,
    });

    if (isOpen) {
      if (channel.children.length) {
        channel.children.forEach((child) => {
          result.push({
            type: RowType.Child,
            id: child.id,
            channel: child,
          });
        });
      } else {
        result.push({
          type: RowType.EmptyChild,
          id: `empty_child_${channel.id}`,
        });
      }
    }

    return result;
  }, [] as RowDefinition[]);
}
