import * as React from "react";
import { useCallback, useEffect, useMemo, useState } from "react";

import { Form, Select } from "components/input";
import { Column } from "components/table/Column";
import { HierarchicalRow, HierarchicalTable } from "components/table/HierarchicalTable";
import { SearchField } from "components/table/SearchField";

import { FlatChannel } from "./types";

// Extended type that includes HierarchicalRow requirements
type ChannelWithHierarchy = FlatChannel &
  HierarchicalRow & {
    synced?: boolean;
  };

type ChannelTableProps = {
  channels: FlatChannel[];
  onSyncStatusChange?: (channelId: number, synced: boolean) => void;
  onChannelSelect?: (channel: FlatChannel) => void;
  loading?: boolean;
  initialSyncStates?: Record<number, boolean>;
};

const ChannelHierarchicalTable: React.FC<ChannelTableProps> = ({
  channels,
  onSyncStatusChange,
  onChannelSelect,
  loading = false,
  initialSyncStates = {},
}) => {
  // Sync status state
  const [syncedChannels, setSyncedChannels] = useState<Record<number, boolean>>(initialSyncStates);
  // Architecture filtering state
  const [selectedArchs, setSelectedArchs] = useState<string[]>([]);

  // Process channels to have proper hierarchical structure, useMemo to avoid recalculation
  const hierarchicalData = useMemo(() => {
    // Build lookup map by channel label
    const channelMap: Record<string, number> = {};
    channels.forEach((channel, index) => {
      channelMap[channel.channelLabel] = index;
    });
    // Create copy with added hierarchical properties
    return channels.map((channel) => {
      // Find parent ID based on parentChannelLabel
      const parentId =
        channel.parentChannelLabel && channelMap[channel.parentChannelLabel] !== undefined
          ? channels[channelMap[channel.parentChannelLabel]].channelId
          : null;
      // Create a hierarchical row that satisfies both Channel and HierarchicalRow types
      return {
        ...channel,
        id: channel.channelId,
        parentId,
        synced: syncedChannels[channel.channelId] || false,
      };
    });
  }, [channels, syncedChannels]);

  // Initialize sync statuses if needed
  useEffect(() => {
    const needsInitialization = channels.some((channel) => syncedChannels[channel.channelId] === undefined);
    if (needsInitialization) {
      setSyncedChannels((prev) => {
        const updatedStatus = { ...prev };
        channels.forEach((channel) => {
          if (updatedStatus[channel.channelId] === undefined) {
            updatedStatus[channel.channelId] = initialSyncStates[channel.channelId] || false;
          }
        });
        return updatedStatus;
      });
    }
  }, [channels, initialSyncStates, syncedChannels]);

  // Handle sync status change - memoized to avoid recreating on each render
  const handleSyncChange = useCallback(
    (channelId: number, synced: boolean) => {
      setSyncedChannels((prev) => ({
        ...prev,
        [channelId]: synced,
      }));

      if (onSyncStatusChange) {
        onSyncStatusChange(channelId, synced);
      }
    },
    [onSyncStatusChange]
  );

  // Handle architecture filter changes
  const handleArchFilterChange = useCallback((selectedOptions: any) => {
    const selectedValues = Array.isArray(selectedOptions) ? selectedOptions.map((option) => option.value) : [];
    setSelectedArchs(selectedValues);
  }, []);

  // Render the organization name - memoized
  const renderOrgCell = useCallback((row: ChannelWithHierarchy) => {
    return row.channelOrg ? row.channelOrg.orgName : "SUSE";
  }, []);

  // Render the sync checkbox - memoized
  const renderSyncCell = useCallback(
    (row: ChannelWithHierarchy) => {
      return (
        <input
          type="checkbox"
          checked={syncedChannels[row.channelId] || false}
          onChange={(e) => handleSyncChange(row.channelId, e.target.checked)}
        />
      );
    },
    [syncedChannels, handleSyncChange]
  );

  // Identifier function for the hierarchical table
  const identifier = useCallback((row: HierarchicalRow): string | number => {
    return row.id;
  }, []);

  // Row class based on sync status - memoized
  const rowClass = useCallback(
    (row: any) => {
      const channel = row as ChannelWithHierarchy;
      return syncedChannels[channel.channelId] ? "synced-channel" : "";
    },
    [syncedChannels]
  );

  // Render channel label cell (required to fix type issues)
  const renderChannelLabelCell = useCallback((row: any) => {
    const channel = row as ChannelWithHierarchy;
    return channel.channelLabel;
  }, []);

  // Render architecture cell (required to fix type issues)
  const renderArchCell = useCallback((row: any) => {
    const channel = row as ChannelWithHierarchy;
    return channel.channelArch;
  }, []);

  // Function to get distinct architectures from channels
  const getDistinctArchsFromData = useCallback((channels: FlatChannel[]) => {
    const archSet = new Set<string>();
    channels.forEach((channel) => archSet.add(channel.channelArch));
    return Array.from(archSet).map((arch) => ({
      value: arch,
      label: arch,
    }));
  }, []);

  // Architecture filter component
  const archFilter = useMemo(
    () => (
      <div className="multiple-select-wrapper table-input-search">
        <Form>
          <Select
            name="channel-arch-filter"
            placeholder={t("Filter by architecture")}
            options={getDistinctArchsFromData(channels)}
            isMulti={true}
            onChange={handleArchFilterChange}
          />
        </Form>
      </div>
    ),
    [channels, getDistinctArchsFromData]
  );

  // Create a searchField with filter for channels
  const searchField = useMemo(
    () => (
      <SearchField
        placeholder={t("Search channels...")}
        filter={(row, criteria) => {
          if (!criteria) return true;
          const searchTerm = criteria.toLowerCase();
          if (selectedArchs.length === 0) {
            return row.channelLabel.toLowerCase().includes(searchTerm);
          } else {
            return selectedArchs.indexOf(row.channelArch) !== -1 && row.channelLabel.toLowerCase().includes(searchTerm);
          }
        }}
      />
    ),
    []
  );

  return (
    <div className="channel-hierarchy-container">
      {/* Search field outside the table */}
      <HierarchicalTable
        data={hierarchicalData}
        identifier={identifier}
        expandColumnKey="channelLabel"
        initiallyExpanded={true}
        cssClassFunction={rowClass}
        initialSortColumnKey="channelLabel"
        initialSortDirection={1}
        searchField={searchField}
        additionalFilters={[archFilter]}
      >
        <Column columnKey="synced" header={t("Sync")} cell={renderSyncCell} width="60px" />
        <Column columnKey="channelLabel" header={t("Channel Label")} cell={renderChannelLabelCell} sortable={true} />
        <Column columnKey="channelArch" header={t("Architecture")} cell={renderArchCell} sortable={true} />
        <Column columnKey="channelOrg" header={t("Organization")} cell={renderOrgCell} sortable={true} />
      </HierarchicalTable>
    </div>
  );
};

export default ChannelHierarchicalTable;
