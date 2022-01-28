import * as React from "react";
import { memo, useEffect, useState, useCallback } from "react";
import debounce from "lodash/debounce";
import xor from "lodash/xor";

import { Loading } from "components/utils/Loading";
import { Select } from "components/input/Select";
import { VirtualList } from "components/virtual-list";
import { ProjectSoftwareSourceType } from "manager/content-management/shared/type";

import styles from "./channels-selection.css";
import { getInitialFiltersState } from "./channels-filters-types";
import BaseChannel from "./base-channel";
import ChannelsFilters from "./channels-filters";
import { useChannelsWithMandatoryApi, useLoadSelectOptions } from "./channels-api";
import { BaseRowDefinition, ChildRowDefinition } from "./channels-selection-rows";

import Worker from "./channels-selection.worker.ts";
import WorkerMessages from "./channels-selection-messages";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedSources: ProjectSoftwareSourceType[];
  // For some reason, the wrapper expects labels, not channels, but that's fine by us
  onChange: (channelLabels: string[]) => void;
};

let rowBatchIdentifier: undefined | number = undefined;
let rowBuffer: BaseRowDefinition[] = [];

const ChannelsSelection = (props: PropsType) => {
  const [isLoading, setIsLoading] = useState(true);
  const [loadSelectOptions] = useLoadSelectOptions();
  const [channelsWithMandatoryPromise] = useChannelsWithMandatoryApi();

  const [worker] = useState(new Worker());
  const [rows, setRows] = useState<BaseRowDefinition[] | undefined>(undefined);
  const [selectedRows, setSelectedRows] = useState<Set<number>>(new Set());
  const [openRows, setOpenRows] = useState<Set<number>>(new Set());
  const [activeFilters, setActiveFilters] = useState<string[]>(getInitialFiltersState());
  const [search, setSearch] = useState("");

  const onSelectedBaseChannelIdChange = (channelId: number) => {
    worker.postMessage({ type: WorkerMessages.SET_SELECTED_BASE_CHANNEL_ID, selectedBaseChannelId: channelId });
    // TODO: Fix
    // onToggleChannelSelect(channelId);
  };

  // Debounce searching so the worker is not overloaded during typing when working with large data sets
  const onSearch = useCallback(
    debounce((newSearch: string) => {
      worker.postMessage({ type: WorkerMessages.SET_SEARCH, search: newSearch });
    }, 50),
    []
  );

  const onToggleChannelSelect = (channel: BaseRowDefinition | ChildRowDefinition, toState?: boolean) => {
    if (toState === false || (typeof toState === "undefined" && selectedRows.has(channel.id))) {
      selectedRows.delete(channel.id);
      channel.requiredBy.forEach((id) => selectedRows.delete(id));
      setSelectedRows(new Set([...selectedRows]));
    } else {
      selectedRows.add(channel.id);
      channel.requires.forEach((id) => selectedRows.add(id));
      setSelectedRows(new Set([...selectedRows]));
    }
  };

  const onToggleChannelOpen = (channelId: number, rowIndex: number) => {
    if (openRows.has(channelId)) {
      openRows.delete(channelId);
      setOpenRows(new Set([...openRows]));
    } else {
      setOpenRows(new Set([...openRows, channelId]));
    }
  };

  useEffect(() => {
    // Ensure the worker knows about our initial configuration
    worker.postMessage({ type: WorkerMessages.SET_ACTIVE_FILTERS, activeFilters });

    channelsWithMandatoryPromise.then(({ channels, mandatoryChannelsMap }) => {
      if (isLoading) {
        setIsLoading(false);
      }

      worker.postMessage({
        type: WorkerMessages.SET_CHANNELS,
        channels,
        mandatoryChannelsMap,
        // These will hold no values if the initial selection is empty
        initialSelectedBaseChannelId: props.initialSelectedSources[0]?.channelId,
        initialSelectedChannelIds: props.initialSelectedSources.map((channel) => channel.channelId),
      });
    });

    worker.addEventListener("message", async ({ data }) => {
      switch (data.type) {
        case WorkerMessages.ROWS_AVAILABLE: {
          if (!Array.isArray(data.rows)) {
            throw new RangeError("Received no valid rows");
          }
          if (typeof data.batchIdentifier === "undefined") {
            throw new TypeError("No batch identifier");
          }
          if (data.batchIdentifier === rowBatchIdentifier) {
            // Append rows to an existing batch
            rowBuffer.push(...data.rows);
          } else {
            // New batch of rows
            rowBatchIdentifier = data.batchIdentifier;
            rowBuffer = data.rows;
          }
          // If we've received everything from the worker, flush the buffer
          if (rowBuffer.length === data.rowCount) {
            setRows(rowBuffer);
            onToggleChannelSelect(rowBuffer[0]);
            rowBuffer = [];
          }
          // TODO: onToggleChannelSelect with the first row if conditions?
          // TODO: Implement
          // props.onChange(data.selectedChannelLabels);
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

  const Row = (channel: BaseRowDefinition, index: number) => {
    return (
      <BaseChannel
        rowDefinition={channel}
        search={search}
        openRows={openRows}
        selectedRows={selectedRows}
        onToggleChannelSelect={(selfOrChild) => onToggleChannelSelect(selfOrChild)}
        onToggleChannelOpen={(channelId) => onToggleChannelOpen(channelId, index)}
      />
    );
  };

  if (isLoading || props.isSourcesApiLoading) {
    return (
      <div className="form-group">
        <Loading text={props.isSourcesApiLoading ? "Adding sources..." : "Loading.."} />
      </div>
    );
  }

  const defaultValueOption = props.initialSelectedSources[0]
    ? {
        base: props.initialSelectedSources[0],
      }
    : undefined;

  return (
    <React.Fragment>
      <div className="row">
        <Select
          loadOptions={loadSelectOptions}
          defaultValueOption={defaultValueOption}
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
              {`${t("Child Channels")} (${selectedRows.size})`}
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
          <VirtualList items={rows} renderItem={Row} />
        </div>
      )}
    </React.Fragment>
  );
};

// This whole view is expensive with large lists, so rerender only when we really need to
export default memo(ChannelsSelection, (prevProps, nextProps) => {
  return (
    prevProps.isSourcesApiLoading === nextProps.isSourcesApiLoading &&
    // This prop is a filtet result but memoed so this works fine
    prevProps.initialSelectedSources === nextProps.initialSelectedSources
  );
});
