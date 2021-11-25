import * as React from "react";
import { memo, useMemo, useEffect, useState } from "react";
import debounce from "lodash/debounce";
import xor from "lodash/xor";

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
import { useChannelsApi, useLoadSelectOptions } from "./channels-selection-select-api";

import Network, { JsonResult } from "utils/network";
import Worker from "./channels-selection.worker.ts";
import WorkerMessages from "./channels-selection-messages";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedIds: Array<number>;
  onChange: (channels: ChannelType[]) => void;
};

// TODO: Move somewhere else
type MandatoryChannelsResponse = {
  [key: number]: unknown[] | undefined;
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
  const [isLoading, setIsLoading] = useState(true);

  // TODO: Make sure this gets destroyed correctly
  const [worker] = useState(new Worker());
  // TODO: Make sure this gets destroyed correctly
  const [loadSelectOptions] = useLoadSelectOptions();
  const [channelsPromise] = useChannelsApi();

  const [rows, setRows] = useState<unknown[] | undefined>(undefined);
  const [search, setSearch] = useState("");
  const [activeFilters, setActiveFilters] = useState<string[]>([]);
  const onSearch = (value: string) => {
    setSearch(value);
    // TODO: Debounce this so we don't overload the worker
    worker.postMessage({ type: WorkerMessages.SET_SEARCH, search: value });
  };
  const [selectedChannelIds] = useState(new Set());

  useEffect(() => {
    // TODO: Move this to a separate file
    channelsPromise.then((channels) => {
      const channelIds = (channels as RawChannelType[]).reduce((ids, channel) => {
        ids.push(channel.base.id, ...channel.children.map((child) => child.id));
        return ids;
      }, [] as number[]);

      return Network.post<JsonResult<MandatoryChannelsResponse>>("/rhn/manager/api/admin/mandatoryChannels", channelIds)
        .then(Network.unwrap)
        .then((mandatoryChannelsMap) => {
          if (isLoading) {
            setIsLoading(false);
          }

          worker.postMessage({ type: WorkerMessages.SET_CHANNELS, channels, mandatoryChannelsMap });
        });
    });

    worker.addEventListener("message", async ({ data }) => {
      switch (data.type) {
        case WorkerMessages.ROWS_CHANGED: {
          if (!Array.isArray(data.rows)) {
            throw new RangeError("Received no valid rows");
          }
          setRows(data.rows);
          return;
        }
        default:
          throw new RangeError(`Unknown message type, got ${data.type}`);
      }
    });

    // When the component unmounts, SIGKILL the worker
    // TODO: Double-triple-quadruple test this
    return () => {
      worker.terminate();
    };
  }, []);

  /*
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
            search={search}
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
            search={search}
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

  */

  if (isLoading || props.isSourcesApiLoading) {
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
          getOptionLabel={(option) => option.base.name}
          getOptionValue={(option) => option.base.id}
          onChange={(name, rawValue) => {
            const value = parseInt(rawValue, 10);
            if (isNaN(value)) {
              return;
            }
            worker.postMessage({ type: WorkerMessages.SET_SELECTED_BASE_CHANNEL_ID, selectedBaseChannelId: value });
          }}
        />
      </div>
      {rows && (
        <div className="row" style={{ display: "flex" }}>
          <label className="col-lg-3 control-label">
            <div className="row" style={{ marginBottom: "30px" }}>
              {`${t("Child Channels")} (${selectedChannelIds.size})`}
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
                activeFilters={activeFilters}
                onChange={(value) => {
                  const newActiveFilters = xor(activeFilters, [value]);
                  setActiveFilters(newActiveFilters);
                  worker.postMessage({ type: WorkerMessages.SET_ACTIVE_FILTERS, activeFilters: newActiveFilters });
                }}
              />
            </div>
          </label>
          {/**
          <VirtualList items={rows} renderRow={Row} rowHeight={rowHeight} />
           */}
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
