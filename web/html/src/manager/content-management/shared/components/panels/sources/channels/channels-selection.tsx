import * as React from "react";
import { memo, useMemo, useEffect, useState } from "react";
import debounce from "lodash/debounce";
import xor from "lodash/xor";

import Network, { JsonResult } from "utils/network";
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
import { ChannelType, DerivedBaseChannel, DerivedChildChannel, RawChannelType } from "core/channels/type/channels.type";
import ChildChannel from "./child-channels";
import RecommendedToggle from "./recommended-toggle";
import ChannelsFilters from "./channels-filters";
import { useChannelsApi, useChannelsWithMandatoryApi, useLoadSelectOptions } from "./channels-selection-api";
import { RowType, RowDefinition } from "./channels-selection-rows";

import Worker from "./channels-selection.worker.ts";
import WorkerMessages from "./channels-selection-messages";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedIds: Array<number>;
  onChange: (channels: ChannelType[]) => void;
};

const ChannelsSelection = (props: PropsType) => {
  const [isLoading, setIsLoading] = useState(true);
  const [loadSelectOptions] = useLoadSelectOptions();
  const [channelsWithMandatoryPromise] = useChannelsWithMandatoryApi();

  const [worker] = useState(new Worker());
  const [rows, setRows] = useState<RowDefinition[] | undefined>(undefined);
  const [selectedChannelsCount, setSelectedChannelsCount] = useState<number>(0);
  const [activeFilters, setActiveFilters] = useState<string[]>([]);

  const [search, setSearch] = useState("");
  const onSearch = (value: string) => {
    setSearch(value);
    // TODO: Debounce this so we don't overload the worker
    worker.postMessage({ type: WorkerMessages.SET_SEARCH, search: value });
  };

  // TODO: This needs to move to the worker just like rows, otherwise selection is ass
  // TODO: What do we need to do when attach/detach is called with previously existing values?
  // See https://dev.to/ganes1410/using-javascript-sets-with-react-usestate-39eo
  // const [[selectedChannelIds], setSelectedChannelIds] = useState<[Set<number>]>([new Set()]);
  const onToggleChannelSelect = (channelId: number, forceSelect?: true) => {
    // TODO: Implement force select and/or untoggle
    worker.postMessage({ type: WorkerMessages.TOGGLE_IS_CHANNEL_SELECTED, channelId, forceSelect });

    /*
    if (forceSelect || !selectedChannelIds.has(channel.id)) {
      selectedChannelIds.add(channel.id);

      // If there's anything required along with the selection, select that as well
      if (channel.mandatory.length) {
        channel.mandatory.forEach((mandatoryChannelId) => selectedChannelIds.add(mandatoryChannelId));
        // If we selected anything additional, open the relevant group too
        // TODO: Implement
      }
    } else {
      selectedChannelIds.delete(channel.id);
      // TODO: Do we need to unselect anything additional?
    }

    setSelectedChannelIds([selectedChannelIds]);
    */
  };

  useEffect(() => {
    channelsWithMandatoryPromise.then(({ channels, mandatoryChannelsMap }) => {
      if (isLoading) {
        setIsLoading(false);
      }

      worker.postMessage({ type: WorkerMessages.SET_CHANNELS, channels, mandatoryChannelsMap });
    });

    worker.addEventListener("message", async ({ data }) => {
      switch (data.type) {
        case WorkerMessages.ROWS_CHANGED: {
          if (!Array.isArray(data.rows) || typeof data.selectedChannelsCount !== "number") {
            throw new RangeError("Received no valid rows");
          }
          setRows(data.rows);
          setSelectedChannelsCount(data.selectedChannelsCount);
          return;
        }
        default:
          throw new RangeError(`Unknown message type, got ${data.type}`);
      }
    });

    // When the component unmounts, SIGKILL the worker
    return () => {
      worker.terminate();
    };
  }, []);

  // TODO: Move this to a component and add padding
  const NoChildren = <span>&nbsp;{t("no child channels")}</span>;
  const Row = (definition: RowDefinition) => {
    switch (definition.type) {
      case RowType.Parent:
        return (
          <ParentChannel
            channel={definition.channel}
            isOpen={definition.isOpen}
            isSelected={definition.isSelected}
            isSelectedBaseChannel={definition.isSelectedBaseChannel}
            selectedChildrenCount={definition.selectedChildrenCount}
            search={search}
            onToggleChannelSelect={(channelId) => onToggleChannelSelect(channelId)}
            onToggleChannelOpen={(channelId) => {
              worker.postMessage({ type: WorkerMessages.TOGGLE_IS_CHANNEL_OPEN, channelId });
            }}
          />
        );
      case RowType.Child:
        return (
          <ChildChannel
            channel={definition.channel}
            isSelected={definition.isSelected}
            search={search}
            onToggleChannelSelect={(channelId) => onToggleChannelSelect(channelId)}
          />
        );
      case RowType.EmptyChild:
        return NoChildren;
      case RowType.RecommendedToggle:
        return (
          <RecommendedToggle
            channel={definition.channel}
            onToggleRecommended={(enable) => {
              // TODO: Implement
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
      case RowType.Parent:
        return 30;
      case RowType.Child:
        return 25;
      case RowType.EmptyChild:
        return 25;
      case RowType.RecommendedToggle:
        return 10;
      default:
        throw new RangeError("Incorrect channel render type in height");
    }
  };

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
            // Ensure the new base channel is selected
            onToggleChannelSelect(value, true);
          }}
        />
      </div>
      {rows && (
        <div className="row" style={{ display: "flex" }}>
          <label className="col-lg-3 control-label">
            <div className="row" style={{ marginBottom: "30px" }}>
              {`${t("Child Channels")} (${selectedChannelsCount})`}
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
