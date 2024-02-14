import * as React from "react";
import { memo, useCallback, useEffect, useState } from "react";

import debounce from "lodash/debounce";
import xor from "lodash/xor";

import { ProjectSoftwareSourceType } from "manager/content-management/shared/type";

import { BaseChannelType, ChannelTreeType, ChildChannelType, isBaseChannel } from "core/channels/type/channels.type";

import { Select } from "components/input/Select";
import { Loading } from "components/utils/Loading";
import { VirtualList } from "components/virtual-list";

import BaseChannel from "./base-channel";
import { useChannelsWithMandatoryApi, useLoadSelectOptions } from "./channels-api";
import ChannelsFilters from "./channels-filters";
import { getInitialFiltersState } from "./channels-filters-state";
import ChannelProcessor from "./channels-processor";
import styles from "./channels-selection.module.css";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedSources: ProjectSoftwareSourceType[];
  // For some reason, the wrapper expects labels, not channels or ids, but that's fine by us
  onChange: (channelLabels: string[]) => void;
};

const ChannelsSelection = (props: PropsType) => {
  const [isLoading, setIsLoading] = useState(true);
  const [loadSelectOptions] = useLoadSelectOptions();
  const [channelsWithMandatoryPromise] = useChannelsWithMandatoryApi();

  const [channelProcessor] = useState(new ChannelProcessor());
  const [rows, setRows] = useState<ChannelTreeType[] | undefined>(undefined);

  const initialSelectedChannelIds = props.initialSelectedSources.map((channel) => channel.channelId);
  const [selectedChannelIds, setSelectedChannelIds] = useState<Set<number>>(new Set(initialSelectedChannelIds));

  const initialSelectedBaseChannelId = props.initialSelectedSources[0]?.channelId;
  const [openRows, setOpenRows] = useState<Set<number>>(new Set([initialSelectedBaseChannelId]));

  const [activeFilters, setActiveFilters] = useState<string[]>(getInitialFiltersState());
  const [search, setSearch] = useState("");

  useEffect(() => {
    // Use the initial filter configuration
    channelProcessor.setActiveFilters(activeFilters);

    channelsWithMandatoryPromise.then(({ channels, channelsMap, requiresMap, requiredByMap }) => {
      if (isLoading) {
        setIsLoading(false);
      }

      const initialSelectedBaseChannelId = props.initialSelectedSources[0]?.channelId;
      channelProcessor
        .setChannels(channels, channelsMap, requiresMap, requiredByMap, initialSelectedBaseChannelId)
        .then((newRows) => {
          if (newRows) {
            setRows(newRows);
          }
        });
    });
  }, []);

  const onSelectedBaseChannelIdChange = (channelId: number) => {
    channelProcessor.setSelectedBaseChannelId(channelId).then((newRows) => {
      if (newRows) {
        setRows(newRows);
      }

      // Select the new base along with any recommended children
      const channel = channelProcessor.channelIdToChannel(channelId);
      onToggleChannelSelect(channel, true);
      if (isBaseChannel(channel)) {
        onToggleChannelOpen(channel, true);
        channel.recommendedChildren.forEach((child) => {
          onToggleChannelSelect(child, true);
        });
      }
    });
  };

  const onSearch = useCallback(
    debounce((newSearch: string) => {
      channelProcessor.setSearch(newSearch).then((newRows) => {
        if (newRows) {
          setRows(newRows);
        }

        if (newSearch) {
          // Open all channels when search changes so visible child matches are also visible
          setOpenRows(new Set(newRows?.map((row) => row.base.id)));
        } else if (channelProcessor.selectedBaseChannelId) {
          // When change is cleared, close all besides the selected base
          setOpenRows(new Set([channelProcessor.selectedBaseChannelId]));
        }
      });
    }, 100),
    []
  );

  const onToggleChannelSelect = (channel: BaseChannelType | ChildChannelType, toState?: boolean) => {
    if (typeof toState === "undefined") {
      toState = !selectedChannelIds.has(channel.id);
    }
    if (toState) {
      selectedChannelIds.add(channel.id);
      const requires = channelProcessor.requiresMap.get(channel.id);
      requires?.forEach((item) => selectedChannelIds.add(item.id));
      setSelectedChannelIds(new Set([...selectedChannelIds]));
    } else {
      selectedChannelIds.delete(channel.id);
      const requiredBy = channelProcessor.requiredByMap.get(channel.id);
      requiredBy?.forEach((item) => selectedChannelIds.delete(item.id));
      setSelectedChannelIds(new Set([...selectedChannelIds]));
    }

    // Propagate selection to parent views
    const selectedChannelLabels = channelProcessor.channelIdsToLabels(Array.from(selectedChannelIds));
    props.onChange(selectedChannelLabels);
  };

  const onToggleChannelOpen = (channel: BaseChannelType, toState?: boolean) => {
    if (typeof toState === "undefined") {
      toState = !openRows.has(channel.id);
    }
    if (toState) {
      setOpenRows(new Set([...openRows, channel.id]));
    } else {
      openRows.delete(channel.id);
      setOpenRows(new Set([...openRows]));
    }
  };

  const Row = (channel: ChannelTreeType) => {
    return (
      <BaseChannel
        rowDefinition={channel}
        search={search}
        openRows={openRows}
        selectedRows={selectedChannelIds}
        selectedBaseChannelId={channelProcessor.selectedBaseChannelId}
        channelProcessor={channelProcessor}
        onToggleChannelSelect={(selfOrChild, toState) => onToggleChannelSelect(selfOrChild, toState)}
        onToggleChannelOpen={(channelId) => onToggleChannelOpen(channelId)}
      />
    );
  };

  if (isLoading || props.isSourcesApiLoading) {
    return (
      <div className="form-group">
        <Loading text={props.isSourcesApiLoading ? "Adding sources..." : "Loading..."} />
      </div>
    );
  }

  const initialSource = props.initialSelectedSources[0];
  const defaultValueOption = initialSource
    ? {
        base: {
          id: initialSource.channelId,
          name: initialSource.name,
        },
      }
    : undefined;

  return (
    <React.Fragment>
      <div className="row">
        <Select
          data-testid="selectedBaseChannel"
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
          <div className="col-lg-3 control-label">
            <label className="row" style={{ marginBottom: "30px" }}>
              {`${t("Child Channels")} (${selectedChannelIds.size})`}
            </label>
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
                  channelProcessor.setActiveFilters(newActiveFilters).then((newRows) => {
                    if (newRows) {
                      setRows(newRows);
                    }
                  });
                }}
              />
            </div>
          </div>
          <VirtualList items={rows} renderItem={Row} defaultItemHeight={29} itemKey={(row) => row.base.id} />
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
