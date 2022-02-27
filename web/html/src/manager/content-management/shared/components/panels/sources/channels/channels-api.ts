import { useState } from "react";

import { BaseChannelType, ChannelTreeType, ChildChannelType } from "core/channels/type/channels.type";

import { asyncIdleCallback } from "utils";
import Network, { JsonResult } from "utils/network";

import { canonicalizeBase, canonicalizeChild, toCanonicalRequires } from "./channels-api-transforms";

const pageSize = window.userPrefPageSize || 15;

const useChannelsApi = () => {
  const [channelsPromise, setChannelsPromise] = useState<Promise<ChannelTreeType[]> | undefined>(undefined);
  if (channelsPromise) {
    return [channelsPromise];
  }

  const promise = Network.get<JsonResult<ChannelTreeType[]>>(`/rhn/manager/api/channels?filterClm=true`).then(
    Network.unwrap
  );
  setChannelsPromise(promise);
  return [promise];
};

type MandatoryChannelsResponse = {
  [key: number]: number[] | undefined;
};
type ChannelsWithMandatory = {
  channels: ChannelTreeType[];
  channelsMap: Map<number, BaseChannelType | ChildChannelType>;
  requiresMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>;
  requiredByMap: Map<number, Set<BaseChannelType | ChildChannelType> | undefined>;
};

export const useChannelsWithMandatoryApi = () => {
  const [responsePromise, setResponsePromise] = useState<Promise<ChannelsWithMandatory> | undefined>(undefined);
  const [channelsPromise] = useChannelsApi();

  if (responsePromise) {
    return [responsePromise];
  }

  const promise = channelsPromise.then(async (channels) => {
    // Gather all channel ids for which we need requirement data
    const channelIds: number[] = [];
    // Keep track of all channels as we go
    const channelsMap: Map<number, BaseChannelType | ChildChannelType> = new Map();

    await asyncIdleCallback(() => {
      // These are regular for-loops since we might be hauling through large volumes of data
      for (let ii = 0; ii < channels.length; ii++) {
        const item = channels[ii];
        const base = item.base;
        channelIds.push(base.id);
        channelsMap.set(base.id, base);
        // Canonicalize the channel as a side-effect
        canonicalizeBase(base);

        for (let jj = 0; jj < item.children.length; jj++) {
          const child = item.children[jj];
          channelIds.push(child.id);
          channelsMap.set(child.id, child);
          if (child.recommended) {
            base.recommendedChildren.push(child);
          }
          // Canonicalize the channel as a side-effect
          canonicalizeChild(child, base);
        }
      }
    });

    return Network.post<JsonResult<MandatoryChannelsResponse>>("/rhn/manager/api/admin/mandatoryChannels", channelIds)
      .then(Network.unwrap)
      .then(async (mandatoryChannelsMap) => {
        const { requiresMap, requiredByMap } = await asyncIdleCallback(() =>
          toCanonicalRequires(mandatoryChannelsMap, channelsMap)
        );
        const result: ChannelsWithMandatory = { channels, channelsMap, requiresMap, requiredByMap };
        return result;
      });
  });
  setResponsePromise(promise);
  return [promise];
};

// This could be moved to the server instead to reduce the overhead
export const useLoadSelectOptions = () => {
  const [channelsPromise] = useChannelsApi();

  const loadSelectOptions = async (searchString: string, previouslyLoaded: BaseChannelType[]) => {
    const channels = await channelsPromise;

    const standardizedSearch = searchString.toLocaleLowerCase();
    const offset = previouslyLoaded.length;
    const filteredChannels = await asyncIdleCallback(() =>
      searchString
        ? channels.filter((channel) => channel.base.name.toLocaleLowerCase().includes(standardizedSearch))
        : channels
    );
    // This is what we would expect to get back from the server given a search string and offset
    const options = filteredChannels.slice(offset, offset + pageSize);
    const hasMore = previouslyLoaded.length + options.length < filteredChannels.length;
    return {
      options,
      hasMore,
    };
  };

  return [loadSelectOptions];
};
