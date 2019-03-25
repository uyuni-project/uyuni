// @flow
import {useEffect, useState} from 'react';
import Network from "utils/network";
import _keyBy from "lodash/keyBy";
import _cloneDeep from "lodash/cloneDeep"
import type {ChannelType} from "core/type/channels/channels.type";

export type ChannelsTreeType = {
  baseIds: Array<string>,
  channelsById: {[key: string]: ChannelType}
}

export type UseChannelsType = {
  isLoading: boolean,
  channelsTree: ChannelsTreeType,
}

const flattenChannelsTree = (apiChannelData): ChannelsTreeType => {
  const flattenChannelsById = apiChannelData.reduce((flattenChannelsAcc, baseEntry) => {
    const baseEntryWithIdAsString = _cloneDeep(baseEntry);
    baseEntryWithIdAsString.base.id = baseEntryWithIdAsString.base.id.toString();
    baseEntryWithIdAsString.children = baseEntryWithIdAsString.children
      ? baseEntryWithIdAsString.children.map(c => ({...c, id: c.id.toString(), children: []}))
      : [];

    flattenChannelsAcc[baseEntryWithIdAsString.base.id] = {
      ...baseEntryWithIdAsString.base,
      children: baseEntryWithIdAsString.children.map(c => c.id)
    };
    return {
      ...flattenChannelsAcc,
      ..._keyBy(baseEntryWithIdAsString.children, "id")
    };
  }, {})

  return {
    baseIds: apiChannelData.map(baseEntry => baseEntry.base.id.toString()),
    channelsById: flattenChannelsById
  }
}

const useChannels = (): UseChannelsType => {

  const [channelsTree, setChannelsTree] = useState({baseIds: [], channelsById: {}});
  const [isLoading, setIsLoading] = useState(true);

  useEffect(()=> {
    Network.get(`/rhn/manager/api/activation-keys/base-channels/-1/child-channels`)
      .promise.then(data => {
      setChannelsTree(flattenChannelsTree(data.data));
      setIsLoading(false);
    })
  }, [])


  return {
    isLoading,
    channelsTree
  };
};

export default useChannels;
