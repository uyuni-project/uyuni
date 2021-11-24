import * as React from "react";
import { memo, useMemo, useEffect, useState } from "react";
import debounce from "lodash/debounce";

import { Loading } from "components/utils/Loading";
import { Select } from "components/input/Select";
import { ChannelsTreeType } from "core/channels/api/use-channels-tree-api";
import useChannelsTreeApi from "core/channels/api/use-channels-tree-api";
import styles from "./channels-selection.css";
import ParentChannel from "./group-channels";
import { useImmerReducer } from "use-immer";
import { VirtualList } from "components/virtual-list";

import { ActionChannelsSelectionType, StateChannelsSelectionType } from "./channels-selection.state";
import { initialStateChannelsSelection, reducerChannelsSelection } from "./channels-selection.state";
import { UseChannelsType } from "core/channels/api/use-channels-tree-api";
import { getVisibleChannels, isGroupVisible, orderBaseChannels } from "./channels-selection.utils";
import useMandatoryChannelsApi from "core/channels/api/use-mandatory-channels-api";
import { getSelectedChannelsIdsInGroup, hasRecommendedChildren } from "core/channels/utils/channels-state.utils";
import { ChannelType, RawChannelType } from "core/channels/type/channels.type";
import ChildChannel from "./child-channels";
import RecommendedToggle from "./recommended-toggle";
import ChannelsFilters from "./channels-filters";

import Network from "utils/network";
import Worker from "./channels-selection.worker.ts";
import WorkerMessages from "./channels-selection-messages";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedIds: Array<number>;
  onChange: (channels: ChannelType[]) => void;
};

enum ChannelRenderType {
  Parent,
  Child,
  RecommendedToggle,
}

type ParentDefinition = {
  type: ChannelRenderType.Parent;
  channel: ChannelType;
};

type ChildDefinition = {
  type: ChannelRenderType.Child;
  channel: ChannelType;
  parent: ChannelType;
};

type RecommendedToggleDefinition = {
  type: ChannelRenderType.RecommendedToggle;
  parent: ChannelType;
};

type RowDefinition = {
  // This identifier is used as the key in the list
  id: string | number;
} & (ParentDefinition | ChildDefinition | RecommendedToggleDefinition);

const ChannelsSelection = (props: PropsType) => {
  // TODO: All of this is too tangled, refactor these hooks
  const { fetchChannelsTree, isChannelsTreeLoaded, channelsTree }: UseChannelsType = useChannelsTreeApi();
  const { fetchMandatoryChannelsByChannelIds, isDependencyDataLoaded, requiredChannelsResult } =
    useMandatoryChannelsApi();
  const [state, dispatchChannelsSelection]: [StateChannelsSelectionType, (arg0: ActionChannelsSelectionType) => void] =
    useImmerReducer(
      (draft, action) => reducerChannelsSelection(draft, action, channelsTree, requiredChannelsResult),
      initialStateChannelsSelection(props.initialSelectedIds)
    );

  // Reimplement the search variable to reduce full list rerenders
  const [search, setSearch] = useState("");
  const debouncedDispatch = debounce(dispatchChannelsSelection, 100);
  const onSearch = (newSearch: string) => {
    setSearch(newSearch);
    debouncedDispatch({
      type: "search",
      search: newSearch,
    });
  };

  const isAllApiDataLoaded = isChannelsTreeLoaded && isDependencyDataLoaded;

  // TODO: Make sure this gets destroyed correctly?
  useEffect(() => {
    // TODO: This needs to be shared with search etc, the scope is wrong
    let worker: Worker | undefined = new Worker();

    // TODO: Move this to a separate file
    Network.get(`/rhn/manager/api/channels?filterClm=true`)
      .then(Network.unwrap)
      .then((channels) => {
        const channelIds = (channels as RawChannelType[]).reduce((ids, channel) => {
          ids.push(channel.base.id, ...channel.children.map((child) => child.id));
          return ids;
        }, [] as number[]);

        return Network.post("/rhn/manager/api/admin/mandatoryChannels", channelIds)
          .then(Network.unwrap)
          .then((mandatoryChannelsMap) => {
            // TODO: Test this, what if we unmount before we get here etc
            if (!worker) {
              return;
            }
            // console.log(channels);
            worker.postMessage({ type: WorkerMessages.SET_CHANNELS, channels, mandatoryChannelsMap });
          });
      });

    worker.addEventListener("message", async ({ data }) => {
      switch (data.type) {
        case WorkerMessages.VIEW_UPDATED:
          // TODO: Implement, set state based on the view etc
          return;
        default:
          throw new RangeError("Unknown message type");
      }
    });
    return () => {
      worker = undefined;
    };
  }, []);

  useEffect(() => {
    fetchChannelsTree().then((channelsTree: ChannelsTreeType) => {
      // TODO: Can this `Object.values()` call be avoided?
      fetchMandatoryChannelsByChannelIds({ channels: Object.values(channelsTree.channelsById) });

      // TODO: Only for testing
      if (false) {
        const testCount = 5000;
        for (var ii = 0; ii < testCount; ii++) {
          const id = 10000000 + ii;
          channelsTree.baseIds.push(id);
          channelsTree.channelsById[id] = {
            id,
            name: `mock channel ${ii}`,
            label: `mock_channel_${ii}`,
            archLabel: "channel-x86_64",
            custom: true,
            isCloned: false,
            subscribable: true,
            recommended: false,
            children: [],
          };
        }
      }
    });
  }, []);

  useEffect(() => {
    // set lead base channel as first and notify
    const sortedSelectedChannelsId = state.selectedChannelsIds.filter((cId) => cId !== state.selectedBaseChannelId);
    sortedSelectedChannelsId.unshift(state.selectedBaseChannelId);
    isAllApiDataLoaded &&
      props.onChange(
        sortedSelectedChannelsId
          .filter((cId) => channelsTree.channelsById[cId])
          .map((cId) => channelsTree.channelsById[cId])
      );
  }, [state.selectedChannelsIds]);

  // TODO: There's a _lot_ of unnecessary work here, someone familiar with the business logic should look to remove unnecessary full iterations over the data set
  // TODO: All of this logic is currently very tightly coupled, but would be nice to refactor most of it out of here
  // Here and below, memoization is for users who have thousands of channels
  const visibleChannels = useMemo(
    () => getVisibleChannels(channelsTree, state.activeFilters),
    [channelsTree, state.activeFilters]
  );
  // Order all base channels by id and set the lead base channel as first
  const orderedBaseChannels = useMemo(
    () => orderBaseChannels(channelsTree, state.selectedBaseChannelId),
    [channelsTree, state.selectedBaseChannelId]
  );

  const pageSize = window.userPrefPageSize || 15;
  // TODO: Move this to the server instead
  const loadSelectOptions = async (searchString: string, previouslyLoaded: ChannelType[]) => {
    const offset = previouslyLoaded.length;

    const filteredChannels = orderedBaseChannels.filter((channel) =>
      channel.name.toLocaleLowerCase().includes(searchString.toLocaleLowerCase())
    );
    // This is what we would expect to get back from the server given a search string and offset
    const options = filteredChannels.slice(offset, offset + pageSize);
    const hasMore = previouslyLoaded.length + options.length < filteredChannels.length;
    return {
      options,
      hasMore,
    };
  };

  const rows = useMemo(
    () =>
      orderedBaseChannels.reduce((result, baseChannel) => {
        // If this group matches current filters etc, append it
        const selectedChannelsIdsInGroup = getSelectedChannelsIdsInGroup(state.selectedChannelsIds, baseChannel);
        const isVisible = isGroupVisible(
          baseChannel,
          channelsTree,
          visibleChannels,
          selectedChannelsIdsInGroup,
          state.selectedBaseChannelId,
          state.search
        );
        if (!isVisible) {
          return result;
        }

        // TODO: Move all data modification logic to the data fetching layer or sth so it's only done once
        result.push({
          type: ChannelRenderType.Parent,
          id: baseChannel.id,
          channel: baseChannel,
        });

        // If the group is open, append all of its children
        const isGroupOpen = state.openGroupsIds.some(
          (openId) => openId === baseChannel.id || baseChannel.children.includes(openId)
        );
        if (isGroupOpen) {
          // TODO: If no children, push an empty child or w/e and break out early

          // Recommended channels toggle, if applicable
          if (hasRecommendedChildren(baseChannel, channelsTree)) {
            result.push({
              type: ChannelRenderType.RecommendedToggle,
              id: `recommended_for_${baseChannel.id}`,
              parent: baseChannel,
            });
          }

          baseChannel.children.forEach((childId) => {
            // TODO: This lookup would be obsolete if the incoming data logic was untangled
            const childChannel = channelsTree.channelsById[childId];
            result.push({
              type: ChannelRenderType.Child,
              id: childChannel.id,
              channel: childChannel,
              parent: baseChannel,
            });
          });
        }

        return result;
      }, [] as RowDefinition[]),
    [orderedBaseChannels, visibleChannels, state.selectedBaseChannelId, state.selectedChannelsIds, state.search]
  );

  const Row = (definition: RowDefinition) => {
    const parentChannel = definition.type === ChannelRenderType.Parent ? definition.channel : definition.parent;
    const selectedChannelsIdsInGroup = getSelectedChannelsIdsInGroup(state.selectedChannelsIds, parentChannel);

    const isOpen = state.openGroupsIds.some(
      (openId) => openId === parentChannel.id || parentChannel.children.includes(openId)
    );

    switch (definition.type) {
      case ChannelRenderType.Parent:
        return (
          <ParentChannel
            channel={parentChannel}
            search={state.search}
            selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
            selectedBaseChannelId={state.selectedBaseChannelId}
            isOpen={isOpen}
            onChannelToggle={(channelId) => {
              return dispatchChannelsSelection({
                type: "toggle_channel",
                baseId: parentChannel.id,
                channelId,
              });
            }}
            onOpenGroup={(open) =>
              dispatchChannelsSelection({
                type: "open_group",
                baseId: parentChannel.id,
                open,
              })
            }
          />
        );
      case ChannelRenderType.Child:
        return (
          <ChildChannel
            channel={definition.channel}
            parent={parentChannel}
            search={state.search}
            selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
            onChannelToggle={(channelId) => {
              return dispatchChannelsSelection({
                type: "toggle_channel",
                baseId: parentChannel.id,
                channelId,
              });
            }}
            channelsTree={channelsTree}
            requiredChannelsResult={requiredChannelsResult}
          />
        );
      case ChannelRenderType.RecommendedToggle:
        return (
          <RecommendedToggle
            parent={parentChannel}
            channelsTree={channelsTree}
            selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
            setAllRecommentedChannels={(enable) => {
              dispatchChannelsSelection({
                type: "set_recommended",
                baseId: parentChannel.id,
                enable,
              });
            }}
          />
        );
      default:
        throw new RangeError("Incorrect channel render type in renderer");
    }
  };

  const rowHeight = (channel: RowDefinition) => {
    switch (channel.type) {
      // TODO: Update all styles so there's no wrapping allowed
      case ChannelRenderType.Parent:
        return 30;
      case ChannelRenderType.Child:
        return 25;
      case ChannelRenderType.RecommendedToggle:
        return 10;
      default:
        throw new RangeError("Incorrect channel render type in height");
    }
  };

  if (!isAllApiDataLoaded || props.isSourcesApiLoading) {
    return (
      <div className="form-group">
        <Loading text={props.isSourcesApiLoading ? "Adding sources..." : "Loading.."} />
      </div>
    );
  }

  return (
    <React.Fragment>
      <div className="row">
        <Select
          name="selectedBaseChannel"
          loadOptions={loadSelectOptions}
          paginate={true}
          label={t("New Base Channel")}
          labelClass="col-md-3"
          divClass="col-md-8"
          hint={t("Choose the channel to be elected as the new base channel")}
          getOptionLabel={(option) => option.name}
          getOptionValue={(option) => option.id}
          onChange={(name, rawValue) => {
            const value = parseInt(rawValue, 10);
            if (isNaN(value)) {
              return;
            }
            dispatchChannelsSelection({
              type: "lead_channel",
              newBaseId: value,
            });
          }}
        />
      </div>
      {state.selectedBaseChannelId && (
        <div className="row" style={{ display: "flex" }}>
          <label className="col-lg-3 control-label">
            <div className="row" style={{ marginBottom: "30px" }}>
              {`${t("Child Channels")} (${state.selectedChannelsIds.length})`}
            </div>
            <div className="row panel panel-default panel-body text-left">
              <div style={{ position: "relative" }}>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Search a channel"
                  value={search}
                  onChange={(event) => onSearch(event.target.value)}
                />
                <span className={`${styles.search_icon_container} clear`}>
                  <i
                    onClick={() => onSearch("")}
                    className="fa fa-times-circle-o no-margin"
                    title={t("Clear Search")}
                  />
                </span>
              </div>
              <hr />
              <ChannelsFilters
                activeFilters={state.activeFilters}
                dispatchChannelsSelection={dispatchChannelsSelection}
              />
            </div>
          </label>
          <VirtualList items={rows} renderRow={Row} rowHeight={rowHeight} />
        </div>
      )}
    </React.Fragment>
  );
};

// This whole view is expensive with large lists, so rerender only when we really need to
export default memo(ChannelsSelection, (prevProps, nextProps) => {
  return (
    prevProps.isSourcesApiLoading === nextProps.isSourcesApiLoading &&
    prevProps.initialSelectedIds.join() === nextProps.initialSelectedIds.join()
  );
});
