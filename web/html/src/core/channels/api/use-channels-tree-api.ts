import { useState } from "react";
import Network from "utils/network";
import _keyBy from "lodash/keyBy";
import { ChannelType } from "core/channels/type/channels.type";
import produce from "utils/produce";

export const getChannelsTreeValues = (channelsTree: ChannelsTreeType): Array<ChannelType> =>
  Object.values(channelsTree.channelsById);

export type ChannelsTreeType = {
  baseIds: Array<number>;
  channelsById: {
    [key: number]: ChannelType;
  };
};

export type UseChannelsType = {
  isChannelsTreeLoaded: boolean;
  channelsTree: ChannelsTreeType;
  fetchChannelsTree: () => Promise<ChannelsTreeType>;
};

const flattenChannelsTree = (apiChannelData: any[]): ChannelsTreeType => {
  const flattenChannelsById = apiChannelData.reduce((flattenChannelsAcc, baseEntry) => {
    // TODO: Specify types
    const baseEntryNormalized: any = produce(baseEntry, (draft): void => {
      draft.children = draft.children ? draft.children.map(c => ({ ...c, children: [] })) : [];
    });

    flattenChannelsAcc[baseEntryNormalized.base.id] = {
      ...baseEntryNormalized.base,
      children: baseEntryNormalized.children.map(c => c.id),
    };
    return {
      ...flattenChannelsAcc,
      ..._keyBy(baseEntryNormalized.children, "id"),
    };
  }, {});

  return {
    baseIds: apiChannelData.map(baseEntry => baseEntry.base.id),
    channelsById: flattenChannelsById,
  };
};

const useChannelsTreeApi = (): UseChannelsType => {
  const [channelsTree, setChannelsTree] = useState<ChannelsTreeType>({ baseIds: [], channelsById: {} });
  const [isChannelsTreeLoaded, setIsChannelsTreeLoaded] = useState(false);

  const fetchChannelsTree = (): Promise<ChannelsTreeType> => {
    return Network.get(`/rhn/manager/api/channels?filterClm=true`).then(data => {
      const channelsTree = flattenChannelsTree(data.data);
      setChannelsTree(channelsTree);
      setIsChannelsTreeLoaded(true);
      return channelsTree;
    });
  };

  return {
    isChannelsTreeLoaded,
    channelsTree,
    fetchChannelsTree,
  };
};

export default useChannelsTreeApi;
