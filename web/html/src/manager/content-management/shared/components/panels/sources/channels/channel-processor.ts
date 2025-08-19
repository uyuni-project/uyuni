import { BaseChannelType, ChannelTreeType, ChildChannelType } from "core/channels/type/channels.type";

import { asyncIdleCallback } from "utils";

import { filterChannels, getTooltipData } from "./channels-processor-transforms";

export type WorkerPayload = Partial<ChannelProcessor>;

export default class ChannelProcessor {
  /** An array of all base channels after they've passed initial processing */
  channels: ChannelTreeType[] | undefined = undefined;
  /** A map from a channel id to any known channel */
  channelsMap: Map<number, BaseChannelType | ChildChannelType> = new Map();
  /** A map from a channel id to a set of channel ids this channel requires */
  requiresMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined> = new Map();
  /** A map from a channel id to a set of channels that require this channel */
  requiredByMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined> = new Map();
  /** Currently selected base channel id */
  selectedBaseChannelId: number | undefined = undefined;
  /** User search string */
  search = "";
  /** User-selected filters such as vendors, custom, clones */
  activeFilters: string[] = [];

  setChannels(
    channels: ChannelTreeType[],
    channelsMap: Map<number, BaseChannelType | ChildChannelType>,
    requiresMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>,
    requiredByMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>,
    selectedBaseChannelId: number | undefined
  ) {
    return this.produceViewFrom({
      channels,
      channelsMap,
      requiresMap,
      requiredByMap,
      selectedBaseChannelId,
    });
  }

  setSearch(search: string) {
    return this.produceViewFrom({ search });
  }

  setSelectedBaseChannelId(selectedBaseChannelId: number) {
    return this.produceViewFrom({ selectedBaseChannelId });
  }

  setActiveFilters(activeFilters: string[]) {
    return this.produceViewFrom({ activeFilters });
  }

  channelIdToChannel(channelId: number) {
    const channel = this.channelsMap.get(channelId);
    if (!channel) {
      throw new TypeError("Could not find channel");
    }
    return channel;
  }

  /** Channel labels, not ids, are used to propagate changes in parenting views */
  channelIdsToLabels(channelIds: number[]) {
    return channelIds.map((id) => {
      const channel = this.channelIdToChannel(id);
      return channel.label;
    });
  }

  getTooltipData = getTooltipData.bind(this);

  /** Ingest new data if any is available, then produce a projection of the existing data */
  private produceViewFrom = async (payload: WorkerPayload = {}) => {
    // If there's any new data available, store it internally
    this.ingest(payload);

    // If we don't have channels or a selected base channel id yet, there's nothing else to do
    if (typeof this.channels === "undefined" || typeof this.selectedBaseChannelId === "undefined") {
      return;
    }

    /**
     * If channels changed or the chosen base channel has changed, ensure channels are properly sorted
     * The sorting is stored and only updated on changes so we don't resort for other basic operations
     */
    if (typeof payload.channels !== "undefined" || typeof payload.selectedBaseChannelId !== "undefined") {
      this.channels = this.sortChannels(this.channels);
    }

    // Store a reference to pass in since the following is async
    const channels = this.channels;
    return asyncIdleCallback(() => {
      return this.filterBaseChannels(channels);
    });
  };

  private filterBaseChannels = filterChannels.bind(this);

  private ingest(payload: WorkerPayload = {}) {
    Object.assign(this, payload);
  }

  private sortChannels = (channels: ChannelTreeType[]) => {
    return channels.sort((a, b) => {
      // If a base has been selected, sort it to the beginning...
      if (this.selectedBaseChannelId) {
        if (a.base.id === this.selectedBaseChannelId) return -1;
        if (b.base.id === this.selectedBaseChannelId) return +1;
      }
      // ...otherwise sort by id
      return a.base.id - b.base.id;
    });
  };
}
