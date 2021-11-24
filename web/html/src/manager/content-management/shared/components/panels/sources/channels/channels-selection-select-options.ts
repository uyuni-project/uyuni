import Network, { JsonResult } from "utils/network";

import { RawChannelType } from "core/channels/type/channels.type";

const pageSize = window.userPrefPageSize || 15;

// TODO: This is an utter hack, but will suffice for now
let selectOptionsPromise: Promise<RawChannelType[]> | undefined = undefined;
// TODO: Move this to a worker too or to the server instead
export const loadSelectOptions = async (searchString: string, previouslyLoaded: RawChannelType[]) => {
  let rawChannels: RawChannelType[];
  if (typeof selectOptionsPromise === "undefined") {
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
              children: [],
            });
          }
        }
        return channels;
      });
    selectOptionsPromise = promise;
    rawChannels = await promise;
  } else {
    rawChannels = await selectOptionsPromise;
  }

  const offset = previouslyLoaded.length;
  const filteredChannels = rawChannels.filter((channel) =>
    channel.base.name.toLocaleLowerCase().includes(searchString.toLocaleLowerCase())
  );
  // This is what we would expect to get back from the server given a search string and offset
  const options = filteredChannels.slice(offset, offset + pageSize);
  const hasMore = previouslyLoaded.length + options.length < filteredChannels.length;
  return {
    options,
    hasMore,
  };
};
