// @flow
import {useState} from 'react';
import type JsonResult from 'utils/network';
import Network from 'utils/network';
import type {ChannelType} from "core/channels/type/channels.type";
import type {ChannelsDependencies} from "core/channels/utils/channels-dependencies.utils";
import {processChannelDependencies, dependenciesTooltip as dependenciesTooltipInternal} from "core/channels/utils/channels-dependencies.utils";

const msgMap = {
  "base_not_found_or_not_authorized": t("Base channel not found or not authorized."),
  "child_not_found_or_not_authorized": t("Child channel not found or not authorized."),
  "invalid_channel_id": t("Invalid channel id")
};


type FetchMandatoryChannelsProps = {
  base: Object,
  channels: Array<ChannelType>,
}

export type RequiredChannelsResultType = {
  requiredChannels: Map<number, Set<number>>,
  requiredByChannels: Map<number, Set<number>>,
  dependenciesTooltip: Function,
}

export type UseMandatoryChannelsApiReturnType = {
  requiredChannelsResult: RequiredChannelsResultType,
  isDependencyDataLoaded: boolean,
  fetchMandatoryChannelsByChannelIds: Function,
}

const useMandatoryChannelsApi = () : UseMandatoryChannelsApiReturnType => {

  const [messages, setMessages] = useState([]);
  const [requiredChannels, setRequiredChannels] = useState(new Map());
  const [requiredByChannels, setRequiredByChannels] = useState(new Map());
  const [mandatoryChannelsRaw, setMandatoryChannelsRaw] = useState(new Map());
  const [isDependencyDataLoaded, setIsDependencyDataLoaded] = useState(false);

  const fetchMandatoryChannelsByChannelIds = (props: FetchMandatoryChannelsProps) => {
    // fetch dependencies data for all child channels and base channel as well
    const needDepsInfoChannels = props.base && props.base.id !== -1 ?
      [props.base.id, ...props.channels.map(c => c.id)]
      : props.channels.map(c => c.id);

    const mandatoryChannelsNotCached = needDepsInfoChannels.filter((channelId) => !mandatoryChannelsRaw[channelId]);
    if(mandatoryChannelsNotCached.length > 0) {
      Network.post('/rhn/manager/api/admin/mandatoryChannels', JSON.stringify(mandatoryChannelsNotCached), "application/json").promise
        .then((data : JsonResult<Map<number, Array<number>>>) => {
          const allTheNewMandatoryChannelsData = Object.assign({}, mandatoryChannelsRaw, data.data);
          let dependencies : ChannelsDependencies = processChannelDependencies(allTheNewMandatoryChannelsData);

          setMandatoryChannelsRaw(allTheNewMandatoryChannelsData);
          setRequiredChannels(dependencies.requiredChannels);
          setRequiredByChannels(dependencies.requiredByChannels);
          setIsDependencyDataLoaded(true);
        })
        .catch((jqXHR: Object, arg: string = '') => {
          const msg = Network.responseErrorMessage(jqXHR, (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
          setMessages(messages.concat(msg))
        });
    } else {
      setIsDependencyDataLoaded(true);
    }
  }

  const dependenciesTooltip = (channelId: number, channels: Array<ChannelType>) => {
    const resolveChannelNames : Function = (channelIds: Array<number>): Array<?string> => {
      return Array.from(channelIds || new Set())
        .map((channelId: number): ?ChannelType  => channels.find(c => c.id === channelId))
        .filter((channel: ?ChannelType): boolean => channel != null)
        .map((channel: ?ChannelType): ?string => channel && channel.name);
    }
    return dependenciesTooltipInternal(
      resolveChannelNames(requiredChannels.get(channelId)),
      resolveChannelNames(requiredByChannels.get(channelId)));
  }

  return {
    requiredChannelsResult: {
      requiredChannels,
      requiredByChannels,
      dependenciesTooltip,
    },
    isDependencyDataLoaded,
    fetchMandatoryChannelsByChannelIds,
  };
}

export default useMandatoryChannelsApi;
