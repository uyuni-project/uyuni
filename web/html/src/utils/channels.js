 // @flow
 'use strict';

declare function t(msg: string): string;

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

module.exports = {
  dependenciesTooltip: dependenciesTooltip
}
