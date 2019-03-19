// @flow
import React, {useState} from 'react';
import Network from '../../utils/network';
import ChannelUtils from '../../utils/channels';

import type JsonResult from '../../utils/network';
import type {ChannelsDependencies} from '../../utils/channels';

const msgMap = {
  "base_not_found_or_not_authorized": t("Base channel not found or not authorized."),
  "child_not_found_or_not_authorized": t("Child channel not found or not authorized."),
  "invalid_channel_id": t("Invalid channel id")
};

type ChannelDto = {
  id: number,
  name: string,
  custom: boolean,
  subscribable: boolean,
  recommended: boolean
}

type ChildChannelsProps = {
  base: Object,
  channels: Array<ChannelDto>,
}

type UseMandatoryChannelsApiReturn = {
  requiredByChannels: Map<number, Set<number>>,
  dependencyDataAvailable: boolean,
  dependenciesTooltip: Function,
  fetchMandatoryChannelsByChannelIds: Function,
}

const useMandatoryChannelsApi = (props: ChildChannelsProps) : UseMandatoryChannelsApiReturn => {

  const [messages, setMessages] = useState([]);
  const [requiredChannels, setRequiredChannels] = useState(new Map());
  const [requiredByChannels, setRequiredByChannels] = useState(new Map());
  const [mandatoryChannelsRaw, setMandatoryChannelsRaw] = useState(new Map());
  const [dependencyDataAvailable, setDependencyDataAvailable] = useState(false);

  const fetchMandatoryChannelsByChannelIds = () => {
    // fetch dependencies data for all child channels and base channel as well
    const needDepsInfoChannels = props.base && props.base.id != -1 ?
      [props.base.id, ...props.channels.map(c => c.id)]
      : props.channels.map(c => c.id);

    const mandatoryChannelsNotCached = needDepsInfoChannels.filter((channelId) => !mandatoryChannelsRaw[channelId]);
    if(mandatoryChannelsNotCached.length > 0) {
      Network.post('/rhn/manager/api/admin/mandatoryChannels', JSON.stringify(mandatoryChannelsNotCached), "application/json").promise
        .then((data : JsonResult<Map<number, Array<number>>>) => {
          const allTheNewMandatoryChannelsData = Object.assign({}, mandatoryChannelsRaw, data.data);
          let dependencies : ChannelsDependencies = ChannelUtils.processChannelDependencies(allTheNewMandatoryChannelsData);

          setMandatoryChannelsRaw(allTheNewMandatoryChannelsData);
          setRequiredChannels(dependencies.requiredChannels);
          setRequiredByChannels(dependencies.requiredByChannels);
          setDependencyDataAvailable(true);
        })
        .catch((jqXHR: Object, arg: string = '') => {
          const msg = Network.responseErrorMessage(jqXHR, (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
          setMessages(messages.concat(msg))
        });
    } else {
      setDependencyDataAvailable(true);
    }
  }

  const dependenciesTooltip = (channelId: number) => {
    const resolveChannelNames : Function = (channelIds: Array<number>): Array<?string> => {
      return Array.from(channelIds || new Set())
        .map((channelId: number): ?ChannelDto  => props.channels.find(c => c.id == channelId))
        .filter((channel: ?ChannelDto): boolean => channel != null)
        .map((channel: ?ChannelDto): ?string => channel && channel.name);
    }
    return ChannelUtils.dependenciesTooltip(
      resolveChannelNames(requiredChannels.get(channelId)),
      resolveChannelNames(requiredByChannels.get(channelId)));
  }

  return {
    requiredChannels,
    requiredByChannels,
    dependencyDataAvailable,
    dependenciesTooltip,
    fetchMandatoryChannelsByChannelIds,
  };
}

export default useMandatoryChannelsApi;
