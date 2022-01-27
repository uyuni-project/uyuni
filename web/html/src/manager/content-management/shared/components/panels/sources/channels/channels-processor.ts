import { DerivedBaseChannel, DerivedChannel } from "core/channels/type/channels.type";

import { derivedChannelsToRowDefinitions, filterBaseChannels } from "./channels-processor-transforms";

export type WorkerPayload = Partial<ChannelProcessor>;

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
  // openBaseChannelIds: Set<number> = new Set();
  /** Set of currently selected channel ids */
  // selectedChannels: Set<DerivedChannel> = new Set();
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

    const filteredBaseChannels = this.filterBaseChannels(this.baseChannels, payload.search);
    const rows = this.derivedChannelsToRowDefinitions(filteredBaseChannels);

    return {
      rows,
    };
  };

  private filterBaseChannels = filterBaseChannels.bind(this);
  private derivedChannelsToRowDefinitions = derivedChannelsToRowDefinitions.bind(this);

  private ingest(payload: WorkerPayload = {}) {
    Object.assign(this, payload);
  }

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
