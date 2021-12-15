import { DerivedBaseChannel, DerivedChannel, isBaseChannel, isChildChannel } from "core/channels/type/channels.type";

import { derivedChannelsToRowDefinitions, filterBaseChannels } from "./channels-processor-transforms";

export type WorkerPayload = Partial<ChannelProcessor> & {
  // Since selection-deselection involves resolving required channels etc, they have special handling
  select?: number[];
  deselect?: number[];
  open?: number[];
  close?: number[];
};

export default class ChannelProcessor {
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

  /** Ingest new data if any is available, then produce a projection of the existing data */
  produceViewFrom = (payload: WorkerPayload = {}) => {
    // If there's any new data available, store it internally
    this.ingest(payload);

    // If we don't have channels or a selected base channel id yet, there's nothing else to do
    if (typeof this.baseChannels === "undefined" || typeof this.selectedBaseChannelId === "undefined") {
      return;
    }

    /**
     * If channels changed or the chosen base channel has changed, ensure channels are properly sorted
     * The sorting is stored and only updated on changes so we don't resort for other basic operations
     */
    if (typeof payload.baseChannels !== "undefined" || typeof payload.selectedBaseChannelId !== "undefined") {
      this.baseChannels = this.sortChannels(this.baseChannels);
    }

    // Update selections and open-close states
    const { select, deselect, open, close } = payload;
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

    const filteredBaseChannels = this.filterBaseChannels(this.baseChannels, payload.search);
    const rows = this.derivedChannelsToRowDefinitions(filteredBaseChannels);
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

  private filterBaseChannels = filterBaseChannels.bind(this);
  private derivedChannelsToRowDefinitions = derivedChannelsToRowDefinitions.bind(this);

  private ingest(payload: WorkerPayload = {}) {
    const { select, deselect, open, close, ...rest } = payload;
    Object.assign(this, rest);
  }

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
