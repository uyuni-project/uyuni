import * as React from "react";
import { memo, useEffect, useState, useCallback } from "react";
import debounce from "lodash/debounce";
import xor from "lodash/xor";

import { Loading } from "components/utils/Loading";
import { Select } from "components/input/Select";
import styles from "./channels-selection.css";
import BaseChannel from "./base-channel";
import { VirtualList } from "components/virtual-list";

import {
  ActionChannelsSelectionType,
  getInitialFiltersState,
  StateChannelsSelectionType,
} from "./channels-selection.state";
// TODO: Replicate this logic
import { initialStateChannelsSelection } from "./channels-selection.state";
import { ChannelType } from "core/channels/type/channels.type";
import ChildChannel from "./child-channels";
import RecommendedToggle from "./recommended-toggle";
import ChannelsFilters from "./channels-filters";
import { useChannelsWithMandatoryApi, useLoadSelectOptions } from "./channels-selection-api";
import { RowType, RowDefinition } from "./channels-selection-rows";

import Worker from "./channels-selection.worker.ts";
import WorkerMessages from "./channels-selection-messages";

type PropsType = {
  isSourcesApiLoading: boolean;
  // TODO: Implement
  // TODO: These can be bound only _after_ we have passed initial data to the worker
  initialSelectedIds: Array<number>;
  // TODO: Where should this be used?!
  // TODO: This wants labels, not channels as input?
  onChange: (channels: ChannelType[]) => void;
};

const ChannelsSelection = (props: PropsType) => {
  const [isLoading, setIsLoading] = useState(true);
  const [loadSelectOptions] = useLoadSelectOptions();
  const [channelsWithMandatoryPromise] = useChannelsWithMandatoryApi();

  const [worker] = useState(new Worker());
  const [rows, setRows] = useState<RowDefinition[] | undefined>(undefined);
  const [selectedChannelsCount, setSelectedChannelsCount] = useState<number>(0);
  const [activeFilters, setActiveFilters] = useState<string[]>(getInitialFiltersState());
  const [search, setSearch] = useState("");

  const onSelectedBaseChannelIdChange = (channelId: number) => {
    worker.postMessage({ type: WorkerMessages.SET_SELECTED_BASE_CHANNEL_ID, selectedBaseChannelId: channelId });
  };

  // Debounce searching so the worker is not overloaded during typing when working with large data sets
  const onSearch = useCallback(
    debounce((newSearch: string) => {
      worker.postMessage({ type: WorkerMessages.SET_SEARCH, search: newSearch });
    }, 50),
    []
  );

  const onToggleChannelSelect = (channelId: number) => {
    worker.postMessage({ type: WorkerMessages.TOGGLE_IS_CHANNEL_SELECTED, channelId });
  };

  const onSetRecommendedChildrenSelected = (channelId: number, selected: boolean) => {
    worker.postMessage({ type: WorkerMessages.SET_RECOMMENDED_CHILDREN_ARE_SELECTED, channelId, selected });
  };

  const onToggleChannelOpen = (channelId: number) => {
    worker.postMessage({ type: WorkerMessages.TOGGLE_IS_CHANNEL_OPEN, channelId });
  };

  useEffect(() => {
    // Ensure the worker knows about our initial configuration
    worker.postMessage({ type: WorkerMessages.SET_ACTIVE_FILTERS, activeFilters });
    // TODO: What do we need to do when attach/detach is called with previously existing values?

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
        case WorkerMessages.SELECTED_CHANNELS_CHANGED: {
          // TODO: On selection change, fire props.onChange(), see https://github.com/uyuni-project/uyuni/blob/master/web/html/src/manager/content-management/shared/components/panels/sources/channels/channels-selection.tsx#L51
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
    // TODO: Move all required fields into the definition and then just pass that and methods
    switch (definition.type) {
      case RowType.Parent:
        return (
          <BaseChannel
            rowDefinition={definition}
            search={search}
            onToggleChannelSelect={(channelId) => onToggleChannelSelect(channelId)}
            onToggleChannelOpen={(channelId) => onToggleChannelOpen(channelId)}
          />
        );
      case RowType.Child:
        return (
          <ChildChannel
            definition={definition}
            search={search}
            onToggleChannelSelect={(channelId) => onToggleChannelSelect(channelId)}
          />
        );
      case RowType.EmptyChild:
        return NoChildren;
      case RowType.RecommendedToggle:
        return (
          <RecommendedToggle
            definition={definition}
            onSetRecommendedChildrenSelected={(channelId, selected) =>
              onSetRecommendedChildrenSelected(channelId, selected)
            }
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
            onSelectedBaseChannelIdChange(value);
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
                  onChange={(event) => {
                    const newSearch = event.target.value;
                    setSearch(newSearch);
                    onSearch(newSearch);
                  }}
                />
                <span className={`${styles.search_icon_container} clear`}>
                  <i
                    onClick={() => {
                      setSearch("");
                      onSearch("");
                    }}
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
