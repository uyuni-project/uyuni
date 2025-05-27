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

      return true;
    });
  }, [hierarchicalData, selectedArchs]);

  const identifier = useCallback((row: HierarchicalRow): string | number => {
    return row.id;
  }, []);

  const handleArchFilterChange = useCallback((_: string | undefined, selectedOptions: string | string[]) => {
    if (Array.isArray(selectedOptions)) {
      setSelectedArchs(selectedOptions);
    } else if (typeof selectedOptions === "string") {
      setSelectedArchs([selectedOptions]);
    } else {
      setSelectedArchs([]);
    }
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
            options={availableOrgs}
            getOptionValue={(org: Org | null) => org?.orgId.toString() ?? ""}
            getOptionLabel={(org: Org | null) => org?.orgName ?? ""}
            defaultValue={row.selectedPeripheralOrg?.orgId.toString()}
            onChange={(_: string | undefined, orgId: string) => {
              onOrgSelect?.(
                row.channelId,
                availableOrgs.find((org) => org.orgId === Number(orgId))
              );
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
    return Array.from(archSet).map((arch) => ({ value: arch, label: arch }));
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
            defaultValue={selectedArchs}
            onChange={handleArchFilterChange}
          />
        </Form>
      </div>
    ),
    [channels, selectedArchs, getDistinctArchsFromData, handleArchFilterChange]
  );

  const filterByChannelLabel = useCallback((datum: ChannelWithHierarchy, criteria: string | undefined) => {
    if (criteria) {
      return datum.channelLabel.includes(criteria);
    }

    return true;
  }, []);

  const searchField = useMemo(
    () => <SearchField placeholder={t("Search channels...")} filter={filterByChannelLabel} />,
    [filterByChannelLabel]
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
        <Column columnKey="channelOrg" header={t("Sync Org")} cell={renderSyncOrgCell} />
      </HierarchicalTable>
    </div>
  );
};

export default HierarchicalChannelsTable;
