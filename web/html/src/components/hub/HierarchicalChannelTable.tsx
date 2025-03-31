import * as React from "react";
import { useCallback, useEffect, useMemo, useState } from "react";

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
  // Search state
  const [searchCriteria, setSearchCriteria] = useState<string>("");
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

  // Filter data based on search criteria
  const filteredData = useMemo(() => {
    if (!searchCriteria) return hierarchicalData;
    // First, find items that match directly
    const matchingItems = hierarchicalData.filter((channel) => {
      const searchTerm = searchCriteria.toLowerCase();
      return (
        channel.channelName.toLowerCase().includes(searchTerm) ||
        channel.channelLabel.toLowerCase().includes(searchTerm) ||
        channel.channelArch.toLowerCase().includes(searchTerm) ||
        (channel.channelOrg?.orgName || "SUSE").toLowerCase().includes(searchTerm)
      );
    });
    // Create a set of IDs for matching items and their ancestors
    const includedIds = new Set<number>();
    // Helper function to add an item and all its ancestors to the includedIds set
    const addWithAncestors = (channel: ChannelWithHierarchy) => {
      includedIds.add(channel.channelId);
      // Add ancestors recursively
      if (channel.parentId) {
        const parent = hierarchicalData.find((c) => c.channelId === channel.parentId);
        if (parent) {
          addWithAncestors(parent);
        }
      }
    };
    matchingItems.forEach(addWithAncestors);
    return hierarchicalData.filter((channel) => includedIds.has(channel.channelId));
  }, [hierarchicalData, searchCriteria]);

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

  // Handle search change
  const handleSearchChange = useCallback((criteria: string) => {
    setSearchCriteria(criteria);
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

  return (
    <div className="channel-hierarchy-container">
      {/* Search field outside the table */}
      <div className="spacewalk-list-head-addons">
        <SearchField placeholder={t("Search channels...")} onSearch={handleSearchChange} />
      </div>
      <HierarchicalTable
        data={filteredData}
        identifier={identifier}
        expandColumnKey="channelLabel"
        initiallyExpanded={true}
        cssClassFunction={rowClass}
        initialSortColumnKey="channelLabel"
        initialSortDirection={1}
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
