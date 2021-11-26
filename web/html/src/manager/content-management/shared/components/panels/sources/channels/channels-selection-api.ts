import { useState } from "react";
import Network, { JsonResult } from "utils/network";

import { RawChannelType } from "core/channels/type/channels.type";

export const useChannelsApi = () => {
  // This is a hack, but will suffice for now
  const [channelsPromise, setChannelsPromise] = useState<Promise<RawChannelType[]> | undefined>(undefined);
  if (channelsPromise) {
    return [channelsPromise];
  }

  const promise = Network.get<JsonResult<RawChannelType[]>>(`/rhn/manager/api/channels?filterClm=true`)
    .then(Network.unwrap)
    .then((channels) => {
      // TODO: Only for testing
      if (false) {
        const testCount = 5000;
        for (var ii = 0; ii < testCount; ii++) {
          const id = 10000000 + ii;
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
