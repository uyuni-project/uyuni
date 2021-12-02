import produce from "utils/produce";

import { DerivedBaseChannel, DerivedChannel, isBaseChannel, isChildChannel } from "core/channels/type/channels.type";

import { channelsFiltersAvailable } from "./channels-filters-types";
import { derivedChannelsToRowDefinitions } from "./channels-selection-transforms";

export type StateChange = Partial<State> & {
  // Since selection-deselection involves resolving required channels etc, they have special handling
  select?: number[];
  deselect?: number[];
  open?: number[];
  close?: number[];
};

export default class State {
  /** An array of all base channels after they've passed initial processing */
  baseChannels: DerivedBaseChannel[] | undefined = undefined;
  /** A map from a channel id to any known channel */
  channelsMap: Map<number, DerivedChannel> = new Map();
  /** A map from a channel id to a set of channel ids this channel requires */
  requiresMap: Map<number, Set<DerivedChannel> | undefined> = new Map();
  /** A map from a channel id to a set of channels that require this channel */
  requiredByMap: Map<number, Set<DerivedChannel> | undefined> = new Map();
  /** Set of currently open base channel ids */
  openBaseChannelIds: Set<number> = new Set();
  /** Set of currently selected channel ids */
  selectedChannels: Set<DerivedChannel> = new Set();
  /** Currently selected base channel id */
  selectedBaseChannelId: number | undefined = undefined;
  /** User search string */
  search = "";
  /** User-selected filters such as vendors, custom, clones */
  activeFilters: string[] = [];

  // Whenever we receive new inputs or data, compute and return an updated result
  resolveChange = (stateChange: StateChange = {}) => {
    const { select, deselect, open, close, ...rest } = stateChange;
    Object.assign(this, rest);

    // If we don't have channels or a selected base channel id yet, there's nothing to do
    if (typeof this.baseChannels === "undefined" || typeof this.selectedBaseChannelId === "undefined") {
      return;
    }

    /**
     * If channels changed or the chosen base channel has changed, ensure channels are properly sorted
     * We store the sorting and only do this on changes so we don't resort for other basic operations
     */
    if (typeof stateChange.baseChannels !== "undefined" || typeof stateChange.selectedBaseChannelId !== "undefined") {
      this.baseChannels = this.sortChannels(this.baseChannels);
    }

    /**
     * This object wrap-unwrap is only required so we can filter and modify the draft in immer at the same time.
     * Immer doesn't allow you to return a new draft value and modify the draft at the same time since it's usually a bug.
     */
    const { baseChannels } = produce({ baseChannels: this.baseChannels }, (draft): void => {
      // Filter by categories and search strings first if possible so everything else is cheaper
      if (this.activeFilters.length) {
        const filters = this.activeFilters.map((name) => channelsFiltersAvailable[name].isVisible);
        draft.baseChannels = draft.baseChannels.filter((channel) => {
          return filters.some((filter) => filter(channel));
        });
      }

      if (this.search) {
        const search = this.search.toLocaleLowerCase();
        draft.baseChannels = draft.baseChannels.filter((channel) => {
          // NB! We filter the children as a side-effect here, sorry
          channel.children = channel.children.filter((child) => {
            console.log(child.standardizedName, child);
            return child.standardizedName.includes(search);
          });
          // If the base channel name matches search or we have any children left after the above, include the base channel
          const matchesSearch = channel.standardizedName.includes(search) || channel.children.length > 0;

          // If the search _changed_ then open base channels that match, otherwise leave user's selection be
          if (typeof stateChange.search !== "undefined") {
            if (matchesSearch) {
              this.openBaseChannelIds.add(channel.id);
            } else {
              this.openBaseChannelIds.delete(channel.id);
            }
          }

          return matchesSearch;
        });
      } else if (typeof stateChange.search !== "undefined") {
        // If the search was cleared, close all base channels
        this.openBaseChannelIds.clear();
      }
    });

    // Update selections and open-close states
    if (typeof select !== "undefined") {
      select.forEach((channelId) => this.selectRecursively(this.channelsMap.get(channelId)));
    }
    if (typeof deselect !== "undefined") {
      deselect.forEach((channelId) => this.deselectRecursively(this.channelsMap.get(channelId)));
    }
    if (typeof open !== "undefined") {
      open.forEach((channelId) => this.openBaseChannelIds.add(channelId));
    }
    if (typeof close !== "undefined") {
      close.forEach((channelId) => this.openBaseChannelIds.delete(channelId));
    }

    const rows = derivedChannelsToRowDefinitions(baseChannels, this);
    const sortedSelectedChannels = this.sortChannels(Array.from(this.selectedChannels));

    return {
      rows,
      selectedChannelsCount: this.selectedChannels.size,
      selectedChannelLabels: sortedSelectedChannels.map((channel) => channel?.label),
    };
  };

  /** Is a channel currently selected */
  isSelected = (channelId: number) => {
    const channel = this.channelsMap.get(channelId);
    return Boolean(channel && this.selectedChannels.has(channel));
  };

  /** Is a base channel currently open */
  isOpen = (channelId: number) => {
    return this.openBaseChannelIds.has(channelId);
  };

  /** Resolve and select all channels that are required along with this channel */
  private selectRecursively = (channel?: DerivedChannel) => {
    if (!channel || this.selectedChannels.has(channel)) {
      return;
    }
    this.selectedChannels.add(channel);

    if (isChildChannel(channel)) {
      // If we selected a child, open the parent
      this.openBaseChannelIds.add(channel.parent.id);
    }

    // Also select any channels that this channel requires
    this.requiresMap.get(channel.id)?.forEach((requiresChannel) => this.selectRecursively(requiresChannel));
  };

  /** Resolve and deselect all channels that require this channel */
  private deselectRecursively = (channel?: DerivedChannel) => {
    if (!channel || !this.selectedChannels.has(channel)) {
      return;
    }
    this.selectedChannels.delete(channel);

    // If we deselected a parent, deselect all of its children
    if (isBaseChannel(channel)) {
      channel.children.forEach((child) => this.deselectRecursively(child));
    }

    // Also deselect any channels that require this channel
    this.requiredByMap.get(channel.id)?.forEach((requiredByChannel) => this.deselectRecursively(requiredByChannel));
  };

  private sortChannels = <T extends DerivedChannel>(channels: T[]) => {
    return channels.sort((a, b) => {
      // If a base has been selected, sort it to the beginning...
      if (this.selectedBaseChannelId) {
        if (a.id === this.selectedBaseChannelId) return -1;
        if (b.id === this.selectedBaseChannelId) return +1;
      }
      // ...otherwise sort by id
      return a.id - b.id;
    });
  };
}
