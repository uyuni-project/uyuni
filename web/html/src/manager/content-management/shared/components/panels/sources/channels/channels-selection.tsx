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
import { getSelectedChannelsIdsInGroup } from "core/channels/utils/channels-state.utils";
import { ChannelType } from "core/channels/type/channels.type";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedIds: Array<number>;
  onChange: (channels: ChannelType[]) => void;
};

const ChannelsSelection = (props: PropsType) => {
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
      if (true) {
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
          <VirtualList />
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
