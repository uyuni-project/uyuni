import * as React from "react";
import { useCallback, useMemo, useState } from "react";

import { Form, Select } from "components/input";
import { Column } from "components/table/Column";
import { HierarchicalRow, HierarchicalTable } from "components/table/HierarchicalTable";
import { SearchField } from "components/table/SearchField";

import { FlatChannel, Org } from "./types";

type ChannelWithHierarchy = FlatChannel &
  HierarchicalRow & {
    isChecked: boolean;
    isPendingAddition?: boolean;
    isPendingRemoval?: boolean;
  };

type ChannelTableProps = {
  channels: FlatChannel[];
  onChannelSelect: (channelId: number, checked: boolean) => void;
  onOrgSelect?: (channelId: number, org?: Org) => void;
  loading?: boolean;
  availableOrgs: Org[];
};

const HierarchicalChannelsTable: React.FC<ChannelTableProps> = ({
  channels,
  onChannelSelect,
  availableOrgs,
  onOrgSelect,
}) => {
  const [selectedArchs, setSelectedArchs] = useState<string[]>([]);
  const [searchCriteria, setSearchCriteria] = useState<string>("");

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

  const handleSearchChange = useCallback((criteria: string) => {
    setSearchCriteria(criteria);
  }, []);

  const identifier = useCallback((row: HierarchicalRow): string | number => {
    return row.id;
  }, []);

  const handleArchFilterChange = useCallback((_, selectedOptions: any) => {
    const selectedValues = Array.isArray(selectedOptions) ? selectedOptions.map((option) => option.value) : [];
    setSelectedArchs(selectedValues);
  }, []);

  const rowClass = useCallback((row: ChannelWithHierarchy) => {
    const isCurrentlySynced = row.synced;
    let className = isCurrentlySynced ? "synced-channel" : "";
    return className;
  }, []);

  const renderSyncCell = useCallback(
    (row: ChannelWithHierarchy) => {
      const channelId = row.channelId;
      // Directly use the pre-calculated checkbox state
      const isChecked = row.isChecked;

      return (
        <div className="d-flex align-items-center">
          <input type="checkbox" checked={isChecked} onChange={() => onChannelSelect(channelId, !isChecked)} />
        </div>
      );
    },
    [onChannelSelect]
  );

  const renderChannelLabelCell = useCallback((row: ChannelWithHierarchy) => {
    return row.channelLabel;
  }, []);

  const renderArchCell = useCallback((row: ChannelWithHierarchy) => {
    return row.channelArch;
  }, []);

  const renderHubOrgCell = useCallback((row: ChannelWithHierarchy) => {
    return row.channelOrg ? row.channelOrg.orgName : "Vendor";
  }, []);

  const orgMapping = availableOrgs.map((org) => ({
    value: org.orgId,
    label: org.orgName,
  }));

  const renderSyncOrgCell = useCallback(
    (row: ChannelWithHierarchy) => {
      if (row.channelOrg === null) {
        return <span>Vendor</span>; // Vendor channels can't sync orgs
      } else if (row.strictOrg && row.selectedPeripheralOrg !== null) {
        // Only 1 option, no choice
        return <span>{row.selectedPeripheralOrg.orgName}</span>;
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
                onOrgSelect(
                  row.channelId,
                  availableOrgs.find((org) => org.orgId === orgId)
                );
              }
            }}
          />
        </Form>
      );
    },
    [availableOrgs, onOrgSelect]
  );

  const getDistinctArchsFromData = useCallback((channels: FlatChannel[]) => {
    const archSet = new Set<string>();
    channels.forEach((channel) => archSet.add(channel.channelArch));
    return Array.from(archSet).map((arch) => ({
      value: arch,
      label: arch,
    }));
  }, []);

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

  const searchField = useMemo(
    () => <SearchField placeholder={t("Search channels...")} onSearch={handleSearchChange} />,
    []
  );

  return (
    <div className="channel-hierarchy-container">
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
