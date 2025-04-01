import * as React from "react";
import { useCallback, useMemo, useState } from "react";

import { Form, Select } from "components/input";
import { Column } from "components/table/Column";
import { HierarchicalRow, HierarchicalTable } from "components/table/HierarchicalTable";
import { SearchField } from "components/table/SearchField";

import { FlatChannel, Org } from "./types";

// Extended type that includes HierarchicalRow requirements
type ChannelWithHierarchy = FlatChannel & HierarchicalRow & { markedForOperation: boolean };

type ChannelTableProps = {
  channels: FlatChannel[];
  onChannelSelect: (channelId: number, chekced: boolean) => void;
  onOrgSelect?: (channelId: number, orgId?: number) => void;
  loading?: boolean;
  availableOrgs: Org[];
};

const HierarchicalChannelsTable: React.FC<ChannelTableProps> = ({
  channels,
  onChannelSelect,
  availableOrgs,
  onOrgSelect,
}) => {
  // Architecture filtering state
  const [selectedArchs, setSelectedArchs] = useState<string[]>([]);
  // Search state
  const [searchCriteria, setSearchCriteria] = useState<string>("");

  // Process channels to have proper hierarchical structure - memoized to avoid recalculation
  const hierarchicalData = useMemo(() => {
    // Build lookup map by channel label
    const channelMap: Record<string, number> = {};
    channels.forEach((channel, index) => {
      channelMap[channel.channelLabel] = index;
    });
    // Create copy with added hierarchical properties
    return channels.map((channel) => {
      const parentId =
        channel.parentChannelLabel && channelMap[channel.parentChannelLabel] !== undefined
          ? channels[channelMap[channel.parentChannelLabel]].channelId
          : null;
      // Create a hierarchical row that satisfies both Channel and HierarchicalRow types
      return {
        ...channel,
        id: channel.channelId,
        parentId,
      };
    });
  }, [channels]);

  // Filter data based on search criteria and architecture filter
  const filteredData = useMemo(() => {
    return hierarchicalData.filter((channel) => {
      // Apply architecture filter
      if (selectedArchs.length > 0 && !selectedArchs.includes(channel.channelArch)) {
        return false;
      }
      // Apply search criteria
      if (searchCriteria) {
        const searchTerm = searchCriteria.toLowerCase();
        return channel.channelLabel.toLowerCase().includes(searchTerm);
      }
      return true;
    });
  }, [hierarchicalData, searchCriteria, selectedArchs]);

  // Handle search change
  const handleSearchChange = useCallback((criteria: string) => {
    setSearchCriteria(criteria);
  }, []);

  // Identifier function for the hierarchical table
  const identifier = useCallback((row: HierarchicalRow): string | number => {
    return row.id;
  }, []);

  // Handle architecture filter changes
  const handleArchFilterChange = useCallback((_, selectedOptions: any) => {
    const selectedValues = Array.isArray(selectedOptions) ? selectedOptions.map((option) => option.value) : [];
    setSelectedArchs(selectedValues);
  }, []);

  // Row class based on sync status and change tracking
  const rowClass = useCallback((row: any) => {
    const channel = row as ChannelWithHierarchy;
    const isCurrentlySynced = channel.synced;
    let className = isCurrentlySynced ? "synced-channel" : "";
    return className;
  }, []);

  // Render the sync checkbox
  const renderSyncCell = useCallback((row: ChannelWithHierarchy) => {
    const channelId = row.channelId;
    const isCurrentlySynced = row.synced;
    const markedForOperation = row.markedForOperation;
    // The checkbox should be checked if:
    // 1. The channel is currently synced AND NOT marked for operation, OR
    // 2. The channel is NOT currently synced BUT marked for operation
    const checked = (isCurrentlySynced && !markedForOperation) || (!isCurrentlySynced && markedForOperation);    return (
      <div className="sync-checkbox-container">
        <input type="checkbox" checked={checked} onChange={(e) => onChannelSelect(channelId, e.target.checked)} />
      </div>
    );
  }, []);

  // Render channel label cell
  const renderChannelLabelCell = useCallback((row: any) => {
    const channel = row as ChannelWithHierarchy;
    return channel.channelLabel;
  }, []);

  // Render architecture cell
  const renderArchCell = useCallback((row: any) => {
    const channel = row as ChannelWithHierarchy;
    return channel.channelArch;
  }, []);

  // Render the organization name - memoized
  const renderHubOrgCell = useCallback((row: ChannelWithHierarchy) => {
    return row.channelOrg ? row.channelOrg.orgName : "SUSE";
  }, []);

  const orgMapping = availableOrgs.map((org) => ({
    value: org.orgId,
    label: org.orgName,
  }));

  const renderSyncOrgCell = useCallback(
    (row: ChannelWithHierarchy) => {
      if (!row.channelOrg) {
        return <span>SUSE</span>; // Vendor channels can't sync orgs
      }
      return (
        <Form>
          <Select
            name={`org-select-${row.channelId}`}
            placeholder={t("Select Organization")}
            isClearable={true}
            options={orgMapping}
            onChange={(_, orgId) => {
              if (onOrgSelect) {
                onOrgSelect(row.channelId, orgId);
              }
            }}
          />
        </Form>
      );
    },
    [availableOrgs, onOrgSelect]
  );

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
    [channels, getDistinctArchsFromData, handleArchFilterChange]
  );

  // Create a searchField with filter for channels
  const searchField = useMemo(
    () => <SearchField placeholder={t("Search channels...")} onSearch={handleSearchChange} />,
    []
  );

  return (
    <div className="channel-hierarchy-container">
      {/* Main table with search and filter */}
      <HierarchicalTable
        data={filteredData}
        identifier={identifier}
        expandColumnKey="channelLabel"
        initiallyExpanded={true}
        cssClassFunction={rowClass}
        searchField={searchField}
        additionalFilters={[archFilter]}
      >
        <Column columnKey="synced" header={t("Sync")} cell={renderSyncCell} width="60px" />
        <Column columnKey="channelLabel" header={t("Channel Label")} cell={renderChannelLabelCell} />
        <Column columnKey="channelArch" header={t("Architecture")} cell={renderArchCell} />
        <Column columnKey="channelOrg" header={t("Hub Org")} cell={renderHubOrgCell} />
        <Column columnKey="channelOrg" header={t("Sync Org")} cell={renderSyncOrgCell} />
      </HierarchicalTable>
    </div>
  );
};

export default HierarchicalChannelsTable;
