import * as React from "react";
import { useEffect } from "react";
import { Loading } from "components/utils/Loading";
import { Select } from "components/input/Select";
import { ChannelsTreeType } from "core/channels/api/use-channels-tree-api";
import useChannelsTreeApi from "core/channels/api/use-channels-tree-api";
import styles from "./channels-selection.css";
import GroupChannels from "./group-channels";
import { useImmerReducer } from "use-immer";
import { VirtualList } from "components/virtual-list";

import { ActionChannelsSelectionType, FilterType, StateChannelsSelectionType } from "./channels-selection.state";
import {
  channelsFiltersAvailableValues,
  initialStateChannelsSelection,
  reducerChannelsSelection,
} from "./channels-selection.state";
import { UseChannelsType } from "core/channels/api/use-channels-tree-api";
import { getVisibleChannels, isGroupVisible, orderBaseChannels } from "./channels-selection.utils";
import useMandatoryChannelsApi from "core/channels/api/use-mandatory-channels-api";
import {
  getAllRecommentedIdsByBaseId,
  getSelectedChannelsIdsInGroup,
  hasRecommendedChildren,
} from "core/channels/utils/channels-state.utils";
import { ChannelType } from "core/channels/type/channels.type";
import ChildChannels from "./child-channels";
import RecommendedToggle from "./recommended-toggle";

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
  // TODO: All of this is too tangled, refactor these api uses
  const { fetchChannelsTree, isChannelsTreeLoaded, channelsTree }: UseChannelsType = useChannelsTreeApi();
  const { fetchMandatoryChannelsByChannelIds, isDependencyDataLoaded, requiredChannelsResult } =
    useMandatoryChannelsApi();
  const [state, dispatchChannelsSelection]: [StateChannelsSelectionType, (arg0: ActionChannelsSelectionType) => void] =
    useImmerReducer(
      (draft, action) => reducerChannelsSelection(draft, action, channelsTree, requiredChannelsResult),
      initialStateChannelsSelection(props.initialSelectedIds)
    );

  const isAllApiDataLoaded = isChannelsTreeLoaded && isDependencyDataLoaded;

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

  if (!isAllApiDataLoaded || props.isSourcesApiLoading) {
    return (
      <div className="form-group">
        <Loading text={props.isSourcesApiLoading ? "Adding sources..." : "Loading.."} />
      </div>
    );
  }

  const onSearch = (search: string) =>
    dispatchChannelsSelection({
      type: "search",
      search,
    });

  const visibleChannels = getVisibleChannels(channelsTree, state.activeFilters);
  // Order all base channels by id and set the lead base channel as first
  let orderedBaseChannels = orderBaseChannels(channelsTree, state.selectedBaseChannelId);

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

  // TODO: Move this into state so we don't recompute this all the time
  const rows = orderedBaseChannels.reduce((result, baseChannel) => {
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

    // TODO: Move all of this to the data layer or sth
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
  }, [] as RowDefinition[]);

  const Row = (definition: RowDefinition) => {
    // TODO: Rename base to parent everywhere?
    const baseChannel = definition.type === ChannelRenderType.Parent ? definition.channel : definition.parent;

    // TODO: This and rest of below should be based on the base, not on the child
    const selectedChannelsIdsInGroup = getSelectedChannelsIdsInGroup(state.selectedChannelsIds, baseChannel);

    const isOpen = state.openGroupsIds.some(
      (openId) => openId === baseChannel.id || baseChannel.children.includes(openId)
    );

    switch (definition.type) {
      case ChannelRenderType.Parent:
        return (
          <GroupChannels
            channel={baseChannel}
            search={state.search}
            selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
            selectedBaseChannelId={state.selectedBaseChannelId}
            isOpen={isOpen}
            onChannelToggle={(channelId) => {
              console.log("toggle", { channelId, baseChannelId: baseChannel.id });
              return dispatchChannelsSelection({
                type: "toggle_channel",
                baseId: baseChannel.id,
                channelId,
              });
            }}
            onOpenGroup={(open) =>
              dispatchChannelsSelection({
                type: "open_group",
                baseId: baseChannel.id,
                open,
              })
            }
            channelsTree={channelsTree}
            requiredChannelsResult={requiredChannelsResult}
          />
        );
      case ChannelRenderType.Child:
        return (
          <ChildChannels
            channel={definition.channel}
            parent={baseChannel}
            search={state.search}
            selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
            onChannelToggle={(channelId) => {
              // TODO: This seems bugged
              console.log("toggle", { channelId, baseChannelId: baseChannel.id });
              return dispatchChannelsSelection({
                type: "toggle_channel",
                baseId: baseChannel.id,
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
            parent={definition.parent}
            channelsTree={channelsTree}
            selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
            setAllRecommentedChannels={(enable) => {
              dispatchChannelsSelection({
                type: "set_recommended",
                baseId: baseChannel.id,
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
    // TODO: Switch based on type
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
        // TODO: Move to styles
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
                  value={state.search}
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
              {/** TODO: Move this out and memo or sth */}
              {channelsFiltersAvailableValues.map((filter: FilterType) => (
                <div key={filter.id} className="checkbox">
                  <input
                    type="checkbox"
                    value={filter.id}
                    checked={state.activeFilters.includes(filter.id)}
                    id={`filter_${filter.id}`}
                    onChange={(event) =>
                      dispatchChannelsSelection({
                        type: "toggle_filter",
                        filter: event.target.value,
                      })
                    }
                  />
                  <label htmlFor={`filter_${filter.id}`}>{filter.text}</label>
                </div>
              ))}
            </div>
          </label>
          {/** className="col-lg-8" */}
          <VirtualList items={rows} renderRow={Row} rowHeight={rowHeight} />
          {/** TODO: Rebuild
              {orderedBaseChannels.map((baseChannel) => {
                const selectedChannelsIdsInGroup = getSelectedChannelsIdsInGroup(
                  state.selectedChannelsIds,
                  baseChannel
                );

                if (
                  !isGroupVisible(
                    baseChannel,
                    channelsTree,
                    visibleChannels,
                    selectedChannelsIdsInGroup,
                    state.selectedBaseChannelId,
                    state.search
                  )
                ) {
                  return null;
                }

                const isOpen = state.openGroupsIds.some(
                  (openId) => openId === baseChannel.id || baseChannel.children.includes(openId)
                );

                return (
                  <GroupChannels
                    key={`group_${baseChannel.id}`}
                    base={baseChannel}
                    search={state.search}
                    childChannelsId={baseChannel.children}
                    selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
                    selectedBaseChannelId={state.selectedBaseChannelId}
                    isOpen={isOpen}
                    setAllRecommentedChannels={(enable) => {
                      dispatchChannelsSelection({
                        type: "set_recommended",
                        baseId: baseChannel.id,
                        enable,
                      });
                    }}
                    onChannelToggle={(channelId) =>
                      dispatchChannelsSelection({
                        type: "toggle_channel",
                        baseId: baseChannel.id,
                        channelId,
                      })
                    }
                    onOpenGroup={(open) =>
                      dispatchChannelsSelection({
                        type: "open_group",
                        baseId: baseChannel.id,
                        open,
                      })
                    }
                    channelsTree={channelsTree}
                    requiredChannelsResult={requiredChannelsResult}
                  />
                );
              })}
              */}
        </div>
      )}
    </React.Fragment>
  );
};

export default ChannelsSelection;
