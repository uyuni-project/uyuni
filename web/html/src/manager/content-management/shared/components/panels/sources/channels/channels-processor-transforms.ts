import { ChannelTreeType } from "core/channels/type/channels.type";

import produce from "utils/produce";

import { channelsFiltersAvailable } from "./channels-filters-state";
import ChannelProcessor from "./channels-processor";

export function getTooltipData(this: ChannelProcessor, channelId: number) {
  const [requiresNames, requiredByNames] = [this.requiresMap.get(channelId), this.requiredByMap.get(channelId)].map(
    (maybeSet) =>
      Array.from(maybeSet || [])
        .filter(Boolean)
        .map((channel) => channel.name)
  );
  return {
    requiresNames,
    requiredByNames,
  };
}

export function filterChannels(this: ChannelProcessor, channels: ChannelTreeType[]) {
  /**
   * This object wrap-unwrap is only required so we can filter and modify the draft in immer at the same time.
   * Immer doesn't allow you to return a new draft value and modify the draft at the same time since it's usually a bug.
   */
  const { filteredChannels } = produce({ filteredChannels: channels }, (draft): void => {
    if (this.activeFilters.length) {
      const filters = this.activeFilters.map((name) => channelsFiltersAvailable[name].isVisible);
      draft.filteredChannels = draft.filteredChannels.filter((channel) => {
        return filters.some((filter) => filter(channel.base));
      });
    }

    if (this.search) {
      const search = this.search.toLocaleLowerCase();
      draft.filteredChannels = draft.filteredChannels.filter((channel) => {
        // NB! We filter the children as a side-effect here, sorry
        channel.children = channel.children.filter((child) => {
          return child.standardizedName.includes(search);
        });

        // If the base channel name matches search or we have any children left after the above, include the base channel
        const matchesSearch = channel.base.standardizedName.includes(search) || channel.children.length > 0;

        return matchesSearch;
      });
    }
  });
  return filteredChannels;
}
