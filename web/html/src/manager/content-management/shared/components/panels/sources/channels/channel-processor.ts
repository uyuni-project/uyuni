import { BaseChannelType, ChannelTreeType, ChildChannelType } from "core/channels/type/channels.type";

import { asyncIdleCallback } from "utils";
import produce from "utils/produce";

import { channelsFiltersAvailable } from "./channels-filters-state";

type InternalProcessorState = {
  /** An array of all base channels after they've passed initial processing */
  channels: ChannelTreeType[] | undefined;
  /** A map from a channel id to any known channel */
  channelsMap: Map<number, BaseChannelType | ChildChannelType>;
  /** A map from a channel id to a set of channel ids this channel requires */
  requiresMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>;
  /** A map from a channel id to a set of channels that require this channel */
  requiredByMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>;
  /** Currently selected base channel id */
  selectedBaseChannelId: number | undefined;
  /** User search string */
  search: string;
  /** User-selected filters such as vendors, custom, clones */
  activeFilters: string[];
};

type WorkerPayload = Partial<InternalProcessorState>;

/**
 * The dependency data of a channel
 */
export type ChannelDependencyData = {
  /** The names of the channels that the channel requires */
  requiresNames: string[];
  /** The names of the channels that require this channel */
  requiredByNames: string[];
};

export class ChannelProcessor {
  private state: InternalProcessorState;

  public constructor() {
    this.state = {
      channels: undefined,
      channelsMap: new Map(),
      requiresMap: new Map(),
      requiredByMap: new Map(),
      selectedBaseChannelId: undefined,
      search: "",
      activeFilters: [],
    };
  }

  /**
   *  Initializes the processor instance with the necessary channel data for processing.
   * @param channels An array representing all available channels.
   * @param channelsMap A map where each channel id is associated with its corresponding object
   * @param requiresMap a mapping detailing for each channel id which other channels it requries
   * @param requiredByMap a mapping detailing for each channel id, which other channels requires it
   * @param selectedBaseChannelId the currently selected base channel id
   * @returns a promise that resolves to the processed array of channels
   */
  public setChannels(
    channels: ChannelTreeType[],
    channelsMap: Map<number, BaseChannelType | ChildChannelType>,
    requiresMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>,
    requiredByMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>,
    selectedBaseChannelId: number | undefined
  ): Promise<ChannelTreeType[] | undefined> {
    return this.produceViewFrom({
      channels,
      channelsMap,
      requiresMap,
      requiredByMap,
      selectedBaseChannelId,
    });
  }

  /**
   * Set the text to be searched among the current channels standardized names
   * @param search the text to search
   * @returns a promise that resolves to the processed array of channels
   */
  public setSearch(search: string): Promise<ChannelTreeType[] | undefined> {
    return this.produceViewFrom({ search });
  }

  /**
   * Set the currently selected base channel id.
   * @param selectedBaseChannelId the base channel id
   * @returns a promise that resolves to the processed array of channels
   */
  public setSelectedBaseChannelId(selectedBaseChannelId: number): Promise<ChannelTreeType[] | undefined> {
    return this.produceViewFrom({ selectedBaseChannelId });
  }

  /**
   * Set the filters selected by the user
   * @param activeFilters the array of filters
   * @returns a promise that resolves to the processed array of channels
   */
  public setActiveFilters(activeFilters: string[]): Promise<ChannelTreeType[] | undefined> {
    return this.produceViewFrom({ activeFilters });
  }

  /**
   * Retrieves the currently selected base channel id, used to connect this processor to an external base selector
   *
   * @returns the id of the currently selected base channel from an external component, if available
   */
  public getSelectedBaseChannelId(): number | undefined {
    return this.state.selectedBaseChannelId;
  }

  /**
   * Retrieves the channel object given its id.
   * @param channelId the identifier of the channel to retrieve.
   * @returns The `BaseChannelType` or `ChildChannelType` instance associated with the given id.
   * @throws {TypeError} If no channel is found for the specified `channelId`.
   */
  public getChannelById(channelId: number): BaseChannelType | ChildChannelType {
    const channel = this.state.channelsMap.get(channelId);
    if (!channel) {
      throw new TypeError("Could not find channel");
    }
    return channel;
  }

  /**
   * Retrieves the labels of the channels corresponding to the given ids These labels are used to propagate changes
   * in parenting views
   *
   * @param channelIds the array of channel ids
   * @return the array of labels
   * @throws {TypeError} If no channel is found for one of the specified id.
   */
  public getChannelLabelsByIds(channelIds: number[]): string[] {
    return channelIds.map((id) => {
      const channel = this.getChannelById(id);
      return channel.label;
    });
  }

  /**
   *  Retrieves the details of the dependencies among channels.
   *
   * @param channelId The identifier of the channel for which to retrieve requirements
   * @returns the dependency data of this channel
   */
  public getDependencyData(channelId: number): ChannelDependencyData {
    const [requiresNames, requiredByNames] = [this.getRequires(channelId), this.getRequiredBy(channelId)].map(
      (maybeSet) =>
        Array.from(maybeSet || [])
          .filter(Boolean)
          .map((channel) => channel.name)
    );

    return { requiresNames, requiredByNames };
  }

  /**
   * Retrieves the set of channel the specified channel requires.
   *
   * @param channelId The identifier of the channel for which to retrieve requirements
   * @returns A Set containing instances of `BaseChannelType` or `ChildChannelType`, or `undefined` if
   * the channel is not found
   */
  public getRequires(channelId: number): Set<BaseChannelType | ChildChannelType> | undefined {
    return this.state.requiresMap.get(channelId);
  }

  /**
   * Retrieves the set of channels that require the specified channel.
   *
   * @param requiringChannelId The identifier of the channel for which to retrieve requirements.
   * @returns A Set containing instances of `BaseChannelType` or `ChildChannelType`, or `undefined` if
   * the channel is not found
   */
  public getRequiredBy(channelId: number): Set<BaseChannelType | ChildChannelType> | undefined {
    return this.state.requiredByMap.get(channelId);
  }

  /** Ingest new data if any is available, then produce a projection of the existing data */
  private async produceViewFrom(payload: WorkerPayload = {}): Promise<ChannelTreeType[] | undefined> {
    // If there's any new data available, store it internally
    this.ingest(payload);

    // If we don't have channels or a selected base channel id yet, there's nothing else to do
    if (typeof this.state.channels === "undefined" || typeof this.state.selectedBaseChannelId === "undefined") {
      return;
    }

    /**
     * If channels changed or the chosen base channel has changed, ensure channels are properly sorted
     * The sorting is stored and only updated on changes so we don't resort for other basic operations
     */
    if (typeof payload.channels !== "undefined" || typeof payload.selectedBaseChannelId !== "undefined") {
      this.state.channels = ChannelProcessor.sortChannels(this.state.channels, this.state.selectedBaseChannelId);
    }

    // Store a reference to pass in since the following is async
    const channels = this.state.channels;
    const search = this.state.search;
    const activeFilters = this.state.activeFilters;

    return asyncIdleCallback(() => ChannelProcessor.filterBaseChannels(channels, search, activeFilters));
  }

  private ingest(payload: WorkerPayload = {}) {
    Object.assign(this.state, payload);
  }

  private static filterBaseChannels(channels: ChannelTreeType[], search: string, activeFilters: string[]) {
    /**
     * This object wrap-unwrap is only required so we can filter and modify the draft in immer at the same time.
     * Immer doesn't allow you to return a new draft value and modify the draft at the same time since it's usually a bug.
     */
    const { filteredChannels } = produce({ filteredChannels: channels }, (draft): void => {
      if (activeFilters.length) {
        const filters = activeFilters.map((name) => channelsFiltersAvailable[name].isVisible);
        draft.filteredChannels = draft.filteredChannels.filter((channel) => {
          return filters.some((filter) => filter(channel.base));
        });
      }

      if (search) {
        draft.filteredChannels = draft.filteredChannels.filter((channel) => {
          // NB! We filter the children as a side-effect here, sorry
          channel.children = channel.children.filter((child) => {
            return child.standardizedName.includes(search.toLocaleLowerCase());
          });

          // If the base channel name matches search or we have any children left after the above, include the base channel
          return channel.base.standardizedName.includes(search.toLocaleLowerCase()) || channel.children.length > 0;
        });
      }
    });

    return filteredChannels;
  }

  private static sortChannels(channels: ChannelTreeType[], selectedBaseId: number | undefined): ChannelTreeType[] {
    return channels.slice().sort((a, b) => {
      // If a base has been selected, sort it to the beginning...
      if (selectedBaseId !== undefined) {
        if (a.base.id === selectedBaseId) {
          return -1;
        }

        if (b.base.id === selectedBaseId) {
          return +1;
        }
      }

      // ...otherwise sort by id
      return a.base.id - b.base.id;
    });
  }
}
