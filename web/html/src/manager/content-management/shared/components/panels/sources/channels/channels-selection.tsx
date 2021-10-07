import * as React from "react";
import Select from "react-select";
import { useEffect } from "react";
import { Loading } from "components/utils/Loading";
import { ChannelsTreeType } from "core/channels/api/use-channels-tree-api";
import useChannelsTreeApi from "core/channels/api/use-channels-tree-api";
import styles from "./channels-selection.css";
import GroupChannels from "./group-channels";
import { useImmerReducer } from "use-immer";

import { ActionChannelsSelectionType, FilterType, StateChannelsSelectionType } from "./channels-selection.state";
import {
  getChannelsFiltersAvailableValues,
  initialStateChannelsSelection,
  reducerChannelsSelection,
} from "./channels-selection.state";
import { UseChannelsType } from "core/channels/api/use-channels-tree-api";
import { getVisibleChannels, isGroupVisible, orderBaseChannels } from "./channels-selection.utils";
import useMandatoryChannelsApi from "core/channels/api/use-mandatory-channels-api";
import { getSelectedChannelsIdsInGroup } from "core/channels/utils/channels-state.utils";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedIds: Array<number>;
  onChange: Function;
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
      fetchMandatoryChannelsByChannelIds({ channels: Object.values(channelsTree.channelsById) });
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

  return (
    <div>
      <div className="form-group">
        <label className="col-lg-3 control-label">{t("New Base Channel")}</label>
        <div className="col-lg-8">
          <Select
            name="selectedBaseChannel"
            id="selectedBaseChannel"
            value={orderedBaseChannels.find((item) => item.id === state.selectedBaseChannelId)}
            onChange={(value) => {
              if (typeof value === "object" && !Array.isArray(value)) {
                dispatchChannelsSelection({
                  type: "lead_channel",
                  newBaseId: parseInt(value.id, 10),
                });
              }
            }}
            options={orderedBaseChannels}
            getOptionLabel={(option) => option.name}
            getOptionValue={(option) => option.id}
            menuPortalTarget={document.body}
            classNamePrefix={`class-selectedBaseChannel`}
            styles={{
              menu: (styles: {}) => ({
                ...styles,
                zIndex: 3,
              }),
              menuPortal: (styles: {}) => ({
                ...styles,
                zIndex: 9999,
              }),
            }}
          />
          <span className="help-block">{t("Choose the channel to be elected as the new base channel")}</span>
        </div>
      </div>
      {state.selectedBaseChannelId && (
        <div className="form-group">
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
              {getChannelsFiltersAvailableValues().map((filter: FilterType) => (
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
          <div className="col-lg-8">
            <div>
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
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChannelsSelection;
