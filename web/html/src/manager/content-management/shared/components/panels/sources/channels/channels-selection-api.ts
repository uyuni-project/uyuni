import { useState } from "react";
import Network, { JsonResult } from "utils/network";

import { RawChannelType } from "core/channels/type/channels.type";

type ChannelsResponse = RawChannelType[];

const MOCK_ID_START = 10000000;

export const useChannelsApi = () => {
  const [channelsPromise, setChannelsPromise] = useState<Promise<ChannelsResponse> | undefined>(undefined);
  if (channelsPromise) {
    return [channelsPromise];
  }

  const promise = Network.get<JsonResult<ChannelsResponse>>(`/rhn/manager/api/channels?filterClm=true`)
    .then(Network.unwrap)
    .then((channels) => {
      // TODO: This matches the current behavior but this should already be done on the server side, with a separate flag if necessary
      channels = channels.sort((a, b) => a.base.id - b.base.id);

      // TODO: Only for testing
      if (false) {
        const testCount = 5000;
        for (var ii = 0; ii < testCount; ii++) {
          const id = MOCK_ID_START + ii;
          channels.push({
            base: {
              id,
              name: `mock channel ${ii}`,
              label: `mock_channel_${ii}`,
              archLabel: "channel-x86_64",
              custom: true,
              isCloned: false,
              subscribable: true,
              recommended: false,
            },
            children: [
              {
                id: id * 2,
                name: `mock channel child ${ii}`,
                label: `mock_channel_child_${ii}`,
                archLabel: "channel-x86_64",
                custom: true,
                isCloned: false,
                subscribable: true,
                recommended: false,
              },
            ],
          });
        }
      }
      return channels;
    });
  setChannelsPromise(promise);
  return [promise];
};

const pageSize = window.userPrefPageSize || 15;

// TODO: Move this to a worker too or to the server instead
export const useLoadSelectOptions = () => {
  const [channelsPromise] = useChannelsApi();

  const loadSelectOptions = async (searchString: string, previouslyLoaded: RawChannelType[]) => {
    const rawChannels = await channelsPromise;

    const offset = previouslyLoaded.length;
    const filteredChannels = searchString
      ? rawChannels.filter((channel) =>
          channel.base.name.toLocaleLowerCase().includes(searchString.toLocaleLowerCase())
        )
      : rawChannels;
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

type MandatoryChannelsResponse = {
  [key: number]: unknown[] | undefined;
};
type ChannelsWithMandatory = { channels: ChannelsResponse; mandatoryChannelsMap: MandatoryChannelsResponse };

export const useChannelsWithMandatoryApi = () => {
  const [responsePromise, setResponsePromise] = useState<Promise<ChannelsWithMandatory> | undefined>(undefined);
  const [channelsPromise] = useChannelsApi();

  if (responsePromise) {
    return [responsePromise];
  }

  const promise = channelsPromise.then((channels) => {
    const channelIds = (channels as RawChannelType[])
      .reduce((ids, channel) => {
        ids.push(channel.base.id, ...channel.children.map((child) => child.id));
        return ids;
      }, [] as number[])
      // TODO: This is only for testing mock values, remove later
      .filter((id) => id < MOCK_ID_START);

    return Network.post<JsonResult<MandatoryChannelsResponse>>("/rhn/manager/api/admin/mandatoryChannels", channelIds)
      .then(Network.unwrap)
      .then((mandatoryChannelsMap) => {
        return { channels, mandatoryChannelsMap };
      });
  });
  setResponsePromise(promise);
  return [promise];
};
