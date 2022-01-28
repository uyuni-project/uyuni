import produce from "utils/produce";

import { DerivedBaseChannel } from "core/channels/type/channels.type";

import {
  ChildRowDefinition,
  EmptyChildRowDefinition,
  RecommendedToggleRowDefinition,
  RowDefinition,
  RowType,
} from "./channels-selection-rows";
import ChannelProcessor from "./channels-processor";
import { channelsFiltersAvailable } from "./channels-filters-types";

function getTooltipData(this: ChannelProcessor, channelId: number) {
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

export function derivedChannelsToRowDefinitions(
  this: ChannelProcessor,
  derivedChannels: DerivedBaseChannel[]
): RowDefinition[] {
  const selectedBaseChannelRequires =
    (this.selectedBaseChannelId && this.requiresMap.get(this.selectedBaseChannelId)) || undefined;

  // TODO: Here and elsewhere, this reduce can become just a regular for loop if we want to go faster
  return derivedChannels.reduce((result, channel) => {
    let children: (ChildRowDefinition | EmptyChildRowDefinition | RecommendedToggleRowDefinition)[] = [];
    let recommendedChildren: ChildRowDefinition[] = [];
    const parentRequires = this.requiresMap.get(channel.id);

    if (channel.children.length) {
      channel.children.forEach((child) => {
        const isRequiredBySelectedBaseChannel = Boolean(selectedBaseChannelRequires?.has(child));
        const childDefinition: ChildRowDefinition = {
          type: RowType.Child,
          id: child.id,
          channelName: child.name,
          isRecommended: child.recommended,
          isRequired: Boolean(parentRequires?.has(child)),
          isRequiredBySelectedBaseChannel,
          tooltipData: getTooltipData.call(this, child.id),
          requires: Array.from(this.requiresMap.get(child.id) || []).map((item) => item.id),
          requiredBy: Array.from(this.requiredByMap.get(child.id) || []).map((item) => item.id),
        };
        children.push(childDefinition);
        if (child.recommended) {
          recommendedChildren.push(childDefinition);
        }
      });
    } else {
      children.push({
        type: RowType.EmptyChild,
        id: `empty_child_${channel.id}`,
      });
    }
    if (recommendedChildren.length) {
      children.unshift({
        type: RowType.RecommendedToggle,
        id: `recommended_toggle_${channel.id}`,
      });
    }

    result.push({
      type: RowType.Parent,
      id: channel.id,
      channelName: channel.name,
      isSelectedBaseChannel: channel.id === this.selectedBaseChannelId,
      requires: Array.from(this.requiresMap.get(channel.id) || []).map((item) => item.id),
      requiredBy: Array.from(this.requiredByMap.get(channel.id) || []).map((item) => item.id),
      children,
      recommendedChildren,
    });

    return result;
  }, [] as RowDefinition[]);
}

export function filterBaseChannels(this: ChannelProcessor, channels: DerivedBaseChannel[], newSearch?: string) {
  /**
   * This object wrap-unwrap is only required so we can filter and modify the draft in immer at the same time.
   * Immer doesn't allow you to return a new draft value and modify the draft at the same time since it's usually a bug.
   */
  const { baseChannels } = produce({ baseChannels: channels }, (draft): void => {
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
          return child.standardizedName.includes(search);
        });

        // If the base channel name matches search or we have any children left after the above, include the base channel
        const matchesSearch = channel.standardizedName.includes(search) || channel.children.length > 0;

        // TODO: Do this in the parent or whatever
        // If the search *changed* then open base channels that match, otherwise leave the user's selection be

        return matchesSearch;
      });
    } else if (typeof newSearch !== "undefined") {
      // If the search was cleared, close all base channels
      // this.openBaseChannelIds.clear();
    }
  });
  return baseChannels;
}
