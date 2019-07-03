// @flow
import {useState} from 'react';
import Network from "utils/network";
import _keyBy from "lodash/keyBy";
import type {ChannelType} from "core/channels/type/channels.type";
import produce from "immer";

// https://github.com/facebook/flow/issues/2221
export const getChannelsTreeValues = (channelsTree: ChannelsTreeType): Array<ChannelType> =>
  (Object.values(channelsTree.channelsById) : any);

export type ChannelsTreeType = {
  baseIds: Array<number>,
  channelsById: {[key: number]: ChannelType}
}

export type UseChannelsType = {
  isChannelsTreeLoaded: boolean,
  channelsTree: ChannelsTreeType,
  fetchChannelsTree: () => Promise<ChannelsTreeType>,
}

const flattenChannelsTree = (apiChannelData): ChannelsTreeType => {
  const flattenChannelsById = apiChannelData.reduce((flattenChannelsAcc, baseEntry) => {

    const baseEntryNormalized = produce(baseEntry, (draft): void  => {
      draft.children = draft.children ? draft.children.map(c => ({...c, children: []})) : [];
    });

    flattenChannelsAcc[baseEntryNormalized.base.id] = {
      ...baseEntryNormalized.base,
      children: baseEntryNormalized.children.map(c => c.id)
    };
    return {
      ...flattenChannelsAcc,
      ..._keyBy(baseEntryNormalized.children, "id")
    };
  }, {})

  return {
    baseIds: apiChannelData.map(baseEntry => baseEntry.base.id),
    channelsById: flattenChannelsById
  }
}

const useChannelsTreeApi = (): UseChannelsType => {

  const [channelsTree, setChannelsTree] = useState({baseIds: [], channelsById: {}});
  const [isChannelsTreeLoaded, setIsChannelsTreeLoaded] = useState(false);

  const fetchChannelsTree = (): Promise<ChannelsTreeType> => {
    return  Network.get(`/rhn/manager/api/activation-keys/base-channels/-1/child-channels`)
      .promise.then(data => {
        const channelsTree = flattenChannelsTree(data.data);
        setChannelsTree(channelsTree);
        setIsChannelsTreeLoaded(true);
        return channelsTree
      })
  }

  return {
    isChannelsTreeLoaded,
    channelsTree,
    fetchChannelsTree
  };
};

export default useChannelsTreeApi;
