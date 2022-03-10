import * as React from "react";
import { useEffect, useState } from "react";

import { MessageType } from "components/messages";

import Network from "utils/network";

const msgMap = {
  base_not_found_or_not_authorized: t("Base channel not found or not authorized."),
  child_not_found_or_not_authorized: t("Child channel not found or not authorized."),
  invalid_channel_id: t("Invalid channel id"),
};

export type Channel = {
  id: number;
  name: string;
  custom: boolean;
  subscribable: boolean;
  recommended: boolean;
};

export type availableChannelsType = Array<{ base: Channel | null | undefined; children: Array<Channel> }>;

type ChildrenArgsProps = {
  messages: any[];
  loading: boolean;
  loadingChildren: boolean;
  availableBaseChannels: Channel[];
  availableChannels: availableChannelsType;
  fetchChildChannels: (baseId: number) => Promise<any>;
};

type ActivationKeyChannelsProps = {
  defaultBaseId: number;
  activationKeyId: number;
  currentSelectedBaseId: number;
  onNewBaseChannel: ({
    currentSelectedBaseId,
    currentChildSelectedIds,
  }: {
    currentSelectedBaseId: number;
    currentChildSelectedIds: number[];
  }) => void;
  children: (arg0: ChildrenArgsProps) => JSX.Element;
};

const ActivationKeyChannelsApi = (props: ActivationKeyChannelsProps) => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [loadingChannels, setLoadingChannels] = useState(true);
  const [loadingBaseChannels, setLoadingBaseChannels] = useState(true);
  const [loadingChildren, setLoadingChildren] = useState(true);
  const [availableBaseChannels, setAvailableBaseChannels] = useState<Channel[]>([]);
  const [availableChannels, setAvailableChannels] = useState<availableChannelsType>([]);
  const [fetchedData, setFetchedData] = useState<Map<number, Array<number>>>(new Map());

  useEffect(() => {
    setLoadingBaseChannels(true);
    fetchBaseChannels().then(() => {
      setLoadingBaseChannels(false);
    });
  }, []);

  useEffect(() => {
    let cancelled = false;
    setLoadingChannels(true);
    fetchActivationKeyChannels(props.activationKeyId)
      .then((response) => {
        if (!response || cancelled) return;

        props.onNewBaseChannel(response);
        setLoadingChildren(true);
        return fetchChildChannels(props.currentSelectedBaseId);
      })
      .then((response) => {
        if (!response || cancelled) return;
        setLoadingChildren(false);
        setAvailableChannels(response.availableChannels);
        setFetchedData(response.fetchedData);
        setLoadingChannels(false);
      });
    return () => {
      cancelled = true;
    };
  }, [props.currentSelectedBaseId]);

  const fetchActivationKeyChannels = async (activationKeyId: number) => {
    if (activationKeyId && activationKeyId !== -1) {
      return Network.get(`/rhn/manager/api/activation-keys/${activationKeyId}/channels`)
        .then((data) => {
          const currentSelectedBaseId = data.data.base ? data.data.base.id : props.defaultBaseId;
          const currentChildSelectedIds = data.data.children ? data.data.children.map((c) => c.id) : [];

          return { currentSelectedBaseId, currentChildSelectedIds };
        })
        .catch(handleResponseError);
    }
  };

  const fetchBaseChannels = () => {
    return Network.get(`/rhn/manager/api/activation-keys/base-channels`)
      .then((data) => {
        setAvailableBaseChannels(Array.from(data.data).map((channel: any) => channel.base));
      })
      .catch(handleResponseError);
  };

  const fetchChildChannels = async (baseId: number) => {
    if (fetchedData && fetchedData.has(baseId)) {
      // TODO: NB!! Either the types are wrong or the logic is wrong here, this comes from the old code but doesn't add up
      // The `as any` cast needs to be removed, but which way the logic needs to be modified needs to be checked first
      setAvailableChannels(fetchedData.get(baseId) as any);
    } else {
      return Network.get(`/rhn/manager/api/activation-keys/base-channels/${baseId}/child-channels`)
        .then((data) => {
          return {
            availableChannels: data.data,
            fetchedData: new Map(fetchedData.set(baseId, data.data)),
          };
        })
        .catch(handleResponseError);
    }
  };

  const handleResponseError = (jqXHR: JQueryXHR, arg: string = "") => {
    const msg = Network.responseErrorMessage(jqXHR, (status, msg) => (msgMap[msg] ? t(msgMap[msg], arg) : null));
    setMessages(messages.concat(msg));
  };

  return props.children({
    messages: messages,
    loading: loadingChannels || loadingBaseChannels,
    loadingChildren: loadingChildren,
    availableBaseChannels: availableBaseChannels,
    availableChannels: availableChannels,
    fetchChildChannels: fetchChildChannels,
  });
};

export default ActivationKeyChannelsApi;
