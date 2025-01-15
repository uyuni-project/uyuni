import { useState } from "react";

import { Channel } from "manager/systems/activation-key/activation-key-channels-api";

import { MandatoryChannel } from "core/channels/type/channels.type";
import { ChannelsDependencies } from "core/channels/utils/channels-dependencies.utils";
import {
  dependenciesTooltip as dependenciesTooltipInternal,
  processChannelDependencies,
} from "core/channels/utils/channels-dependencies.utils";

import { MessageType } from "components/messages/messages";

import { JsonResult } from "utils/network";
import Network from "utils/network";

const messageMap = {
  base_not_found_or_not_authorized: t("Base channel not found or not authorized."),
  child_not_found_or_not_authorized: t("Child channel not found or not authorized."),
  invalid_channel_id: t("Invalid channel id"),
};

type FetchMandatoryChannelsProps = {
  base?: { id: number };
  channels: Array<MandatoryChannel>;
};

export type RequiredChannelsResultType = {
  requiredChannels: Map<number, Set<number>>;
  requiredByChannels: Map<number, Set<number>>;
  dependenciesTooltip: (channelId: number, channels: (MandatoryChannel | Channel)[]) => string | undefined;
};

export type UseMandatoryChannelsApiReturnType = {
  requiredChannelsResult: RequiredChannelsResultType;
  isDependencyDataLoaded: boolean;
  fetchMandatoryChannelsByChannelIds: (props: FetchMandatoryChannelsProps) => void;
};

const useMandatoryChannelsApi = (): UseMandatoryChannelsApiReturnType => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [requiredChannels, setRequiredChannels] = useState(new Map());
  const [requiredByChannels, setRequiredByChannels] = useState(new Map());
  const [mandatoryChannelsRaw, setMandatoryChannelsRaw] = useState(new Map());
  const [isDependencyDataLoaded, setIsDependencyDataLoaded] = useState(false);

  const fetchMandatoryChannelsByChannelIds = (props: FetchMandatoryChannelsProps) => {
    // fetch dependencies data for all child channels and base channel as well
    const needDepsInfoChannels =
      props.base && props.base.id !== -1
        ? [props.base.id, ...props.channels.map((c) => c.id)]
        : props.channels.map((c) => c.id);

    const mandatoryChannelsNotCached = needDepsInfoChannels.filter((channelId) => !mandatoryChannelsRaw[channelId]);
    if (mandatoryChannelsNotCached.length > 0) {
      Network.post("/rhn/manager/api/admin/mandatoryChannels", mandatoryChannelsNotCached)
        .then((data: JsonResult<Map<number, Array<number>>>) => {
          const allTheNewMandatoryChannelsData = Object.assign({}, mandatoryChannelsRaw, data.data);
          let dependencies: ChannelsDependencies = processChannelDependencies(allTheNewMandatoryChannelsData);

          setMandatoryChannelsRaw(allTheNewMandatoryChannelsData);
          setRequiredChannels(dependencies.requiredChannels);
          setRequiredByChannels(dependencies.requiredByChannels);
        })
        .catch((jqXHR: JQueryXHR, arg: string = "") => {
          const msg = Network.responseErrorMessage(jqXHR, (status, msg) =>
            messageMap[msg] ? t(messageMap[msg], arg) : null
          );
          setMessages(messages.concat(msg));
        })
        .finally(() => setIsDependencyDataLoaded(true));
    } else {
      setIsDependencyDataLoaded(true);
    }
  };

  const dependenciesTooltip = (channelId: number, channels: (MandatoryChannel | Channel)[]) => {
    const resolveChannelNames: Function = (channelIds: Array<number>): Array<string | null | undefined> => {
      return Array.from(channelIds || new Set())
        .map((channelId: number) => channels.find((c) => c.id === channelId))
        .filter((channel): boolean => channel != null)
        .map((channel): string | null | undefined => channel && channel.name);
    };
    return dependenciesTooltipInternal(
      resolveChannelNames(requiredChannels.get(channelId)),
      resolveChannelNames(requiredByChannels.get(channelId))
    );
  };

  return {
    requiredChannelsResult: {
      requiredChannels,
      requiredByChannels,
      dependenciesTooltip,
    },
    isDependencyDataLoaded,
    fetchMandatoryChannelsByChannelIds,
  };
};

export default useMandatoryChannelsApi;
