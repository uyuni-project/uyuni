 // @flow
 'use strict';

declare function t(msg: string): string;

// Converts sets of channel names into a human-readable tooltip
// containing information about channel dependencies
function dependenciesTooltip(
  requiredChannels: Set<string>,
  requiredByChannels: Set<string>) : string
{
  const channelLines = (channelNames) => {
    return Array.from(channelNames || new Set())
      .reduce((channelName1, channelName2) => channelName1 + "\n" + channelName2, "");
  }

  const requiredChannelsLines = channelLines(requiredChannels);
  const requiredByChannelsLines = channelLines(requiredByChannels);

  return t("Required channels") + ": \n" + (requiredChannelsLines || "(" + t("none") + ")") + "\n\n"
    + t("Require this channel") + ": \n" + (requiredByChannelsLines || "(" + t("none") + ")");
}

// Given the map of channel dependencies on other channels (i.e. "which channels depend on a channel?")
// compute reverse dependencies (i.e. "on which channels do a channel depend?")
function computeReverseDependencies(dependencyMap : Map<number, Set<number>>) : Map<number, Set<number>>
{
  // merges entry e to the accMap
  const mergeEntries = (accMap, e) => {
      if (accMap.has(e[0])) {
          accMap.get(e[0]).add(e[1]);
      } else {
          accMap.set(e[0], new Set([e[1]]));
      }
      return accMap;
  }

  return Array.from(dependencyMap.keys())
      .flatMap(key => Array.from(dependencyMap.get(key)).map(val => [val, key]))
      .reduce(mergeEntries, new Map());
}

function processChannelDependencies(requiredChannelsRaw) {
  const requiredChannels = new Map(Object.entries(requiredChannelsRaw)
    .map(entry => {
      const channelId = parseInt(entry[0]);
      const requiredChannelList = entry[1];
      return [
        channelId,
        new Set(requiredChannelList.filter(requiredId => requiredId !== channelId))
      ];
    }));

  const requiredByChannels = computeReverseDependencies(requiredChannels);

  return {
    requiredChannels: requiredChannels,
    requiredByChannels: requiredByChannels
  };
}

module.exports = {
  dependenciesTooltip: dependenciesTooltip,
  processChannelDependencies: processChannelDependencies
}
