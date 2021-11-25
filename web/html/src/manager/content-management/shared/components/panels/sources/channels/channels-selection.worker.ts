// TODO: Does all of this work with HMR?

// NB! Workers must import polyfills
// TODO: Find a pattern to ensure this always happens
import "manager/polyfills.ts";

import produce from "utils/produce";

// If we want to use more workers in the future, using a dedicated library such as Comlink or something similar might make sense
import WorkerMessages from "./channels-selection-messages";
import { DerivedBaseChannel, RawChannelType } from "core/channels/type/channels.type";
import { channelsFiltersAvailable } from "./channels-selection.state";

// eslint-disable-next-line no-restricted-globals
const context: Worker = self as any;

const initialState = () => ({
  baseChannels: undefined as DerivedBaseChannel[] | undefined,
  selectedBaseChannelId: undefined as number | undefined,
  // TODO: Do we need this at all?
  // openBaseChannelIds: new Set<number>(),
  search: "",
  activeFilters: [] as string[],
});
const state = initialState();

// Respond to message from parent thread
context.addEventListener("message", async ({ data }) => {
  switch (data.type) {
    // The worker can't currently integrate with our network layer due to its reliance on jQuery, in the future the worker could do the request itself
    case WorkerMessages.SET_CHANNELS: {
      // TODO: Also get required and mandatory etc information and then merge it all together
      if (!Array.isArray(data.channels) || !data.mandatoryChannelsMap) {
        throw new TypeError("Insufficient channel data");
      }

      // TODO: Lift all of this out into a function and add tests
      // TODO: Type all of this
      // NB! The data we receive here is already a copy since we're in a worker so it's safe to modify it directly
      const baseChannels = data.channels.map((rawChannel: RawChannelType) => {
        // TODO: This cast is not correct
        // If we want to reduce copy overhead we could only pick the fields we need here
        const baseChannel: DerivedBaseChannel = rawChannel.base as DerivedBaseChannel;
        baseChannel.isOpen = false;
        // This should always be available, but just in case
        baseChannel.mandatory = data.mandatoryChannelsMap[baseChannel.id] || [];
        // Precompute filtering values so we only do this once
        baseChannel.standardizedName = baseChannel.name.toLocaleLowerCase();

        baseChannel.children = rawChannel.children.map((child) => ({
          ...child,
          parent: baseChannel,
          mandatory: data.mandatoryChannelsMap[child.id] || [],
          standardizedName: child.name.toLocaleLowerCase(),
        }));

        return baseChannel;
      });

      onChange({ baseChannels });
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
    default:
      throw new RangeError(`Unknown message type, got ${data.type}`);
  }
});

// Whenever we receive new inputs or data, compute and return an updated result
function onChange(partialState: Partial<typeof state>) {
  Object.assign(state, partialState);

  // If we don't have channels or a selected base channel id yet, there's nothing to do
  if (typeof state.baseChannels === "undefined" || typeof state.selectedBaseChannelId === "undefined") {
    return;
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
        if (partialState.search) {
          channel.isOpen = matchesSearch;
        }

        return matchesSearch;
      });
    }

    // If channels changed or the selected base changed, ensure channels are properly sorted
    if (partialState.baseChannels || partialState.selectedBaseChannelId) {
      draft.baseChannels = draft.baseChannels.sort((a, b) => {
        // If a base has been selected, sort it to the beginning...
        if (state.selectedBaseChannelId) {
          if (a.id === state.selectedBaseChannelId) return -1;
          if (b.id === state.selectedBaseChannelId) return +1;
        }
        // ...otherwise sort by id
        return a.id - b.id;
      });
    }

    // TODO: Compute visible groups, isOpen, etc

    // TODO: Flatten to a flat list with types for rendering and then return
  });

  console.log(baseChannels);

  context.postMessage({
    type: WorkerMessages.ROWS_CHANGED,
    // TODO: Actual rows
    rows: [],
  });
}
