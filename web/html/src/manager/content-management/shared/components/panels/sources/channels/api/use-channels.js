// @flow
import React, {useEffect, useState} from 'react';
import Network from "../../../../../../../../utils/network";
import _keyBy from "lodash/keyBy";
import _cloneDeep from "lodash/cloneDeep"

type channelsType = {
  baseIds: Array<number>,
  channelsById: {[key: Number]: {}} // TODO: Add shared channel types
}

export type useChannelsType = {
  isLoading: boolean,
  channels: channelsType,
}

const flattenChannelsTree = (apiChannelData): channelsType => {
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

const useChannels = (): useChannelsType => {

  const [channels, setChannels] = useState(null);

  useEffect(()=> {
    Network.get(`/rhn/manager/api/activation-keys/base-channels/-1/child-channels`)
      .promise.then(data => {
      setChannels(flattenChannelsTree(data.data));
    })
  }, [])


  return {
    isLoading: !Boolean(channels),
    channels
  };
};

export default useChannels;
