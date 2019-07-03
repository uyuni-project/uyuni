/* eslint-disable */
// @flow
'use strict';

import type {RequiredChannelsResultType} from "core/channels/api/use-mandatory-channels-api";

export type ChannelsDependencies = {
  requiredChannels: Map<number, Set<number>>,
  requiredByChannels: Map<number, Set<number>>
}

const _union = require("lodash/union");


// Converts array of channel names into a human-readable tooltip
// containing information about channel dependencies
// return null if the channel is not involved in any dependencies
function dependenciesTooltip(
  requiredChannels: Array<string>,
  requiredByChannels: Array<string>) : ?string
{
  if (requiredChannels.length === 0 && requiredByChannels.length === 0) {
    return null;
  }

  const channelLines = (channelNames) => {
    return channelNames
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

function processChannelDependencies(requiredChannelsRaw) : ChannelsDependencies {
  const requiredChannels: Map<number, Set<number>> = new Map(Object.entries(requiredChannelsRaw)
    .map(entry => {
      const channelId = parseInt(entry[0]);
      const requiredChannelList = entry[1];
      return [
        channelId,
        new Set(requiredChannelList.filter(requiredId => requiredId !== channelId))
      ];
    }));

  const requiredByChannels: Map<number, Set<number>> = computeReverseDependencies(requiredChannels);

  return {
    requiredChannels: requiredChannels,
    requiredByChannels: requiredByChannels
  };
}

function getChannelsToToggleWithDependencies(
  channelsId: Array<number>,
  requiredChannelsResult: RequiredChannelsResultType,
  isSelection: boolean
): Array<number> {
  let channelsToToggle: Array<number> = [...channelsId];
  channelsId.forEach((channelId) => {
    if(isSelection) {
      channelsToToggle = _union(channelsToToggle, Array.from(requiredChannelsResult.requiredChannels.get(channelId) || []))
    } else {
      channelsToToggle = _union(channelsToToggle, Array.from(requiredChannelsResult.requiredByChannels.get(channelId) || []))
    }
  });
  return channelsToToggle;
}

module.exports = {
  dependenciesTooltip: dependenciesTooltip,
  processChannelDependencies: processChannelDependencies,
  getChannelsToToggleWithDependencies
}
