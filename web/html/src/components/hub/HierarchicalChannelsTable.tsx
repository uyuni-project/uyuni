import * as React from "react";
import { useCallback, useMemo, useState } from "react";

import { DEPRECATED_Select, Form } from "components/input";
import { Column } from "components/table/Column";
import { HierarchicalRow, DEPRECATED_HierarchicalTable } from "components/table/HierarchicalTable";
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
    const className = isCurrentlySynced ? "synced-channel" : "";
    return className;
  }, []);

  const renderSyncCell = useCallback(
    (row: ChannelWithHierarchy) => {
      const channelId = row.channelId;
      // Directly use the pre-calculated checkbox state
      const isChecked = row.isChecked;

      return (
        <div className="text-center">
          <input
            type="checkbox"
            checked={isChecked}
            onChange={() => {
              // If the current row allows choosing an organization, update the selected organization
              if (row.channelOrg !== null && !row.strictOrg) {
                onOrgSelect?.(channelId, !isChecked ? availableOrgs[0] : undefined);
              }

              onChannelSelect(channelId, !isChecked);
            }}
          />
        </div>
      );
    },
    [onChannelSelect]
  );

  const renderChannelNameCell = useCallback((row: ChannelWithHierarchy) => {
    return row.channelName;
  }, []);

  const renderArchCell = useCallback((row: ChannelWithHierarchy) => {
    return row.channelArch;
  }, []);

  const renderSyncOrgCell = useCallback(
    (row: ChannelWithHierarchy) => {
      if (row.channelOrg === null) {
        // Vendor channels can't sync orgs
        return <span>Vendor</span>;
      }

      if (row.strictOrg && row.selectedPeripheralOrg !== null) {
        // Only 1 option, no choice
        return <span>{row.selectedPeripheralOrg.orgName}</span>;
      }

      if (!row.isChecked) {
        // The row is not selected, do not show anything
        return <span>{"-"}</span>;
      }

      return (
        <Form>
          <DEPRECATED_Select
            className="mb-0"
            name={`org-select-${row.channelId}`}
            placeholder={t("Select Organization")}
            options={availableOrgs}
            getOptionValue={(org: Org) => org.orgId.toString()}
            getOptionLabel={(org: Org) => org.orgName}
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
          <DEPRECATED_Select
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

  const filterByChannelName = useCallback((datum: ChannelWithHierarchy, criteria: string | undefined) => {
    if (criteria) {
      return datum.channelName.includes(criteria);
    }

    return true;
  }, []);

  const searchField = useMemo(
    () => <SearchField placeholder={t("Search channels...")} filter={filterByChannelName} />,
    [filterByChannelName]
  );

  return (
    <DEPRECATED_HierarchicalTable
      className="channel-hierarchy"
      data={filteredData}
      identifier={identifier}
      expandColumnKey="channelName"
      initiallyExpanded={true}
      cssClassFunction={rowClass}
      searchField={searchField}
      additionalFilters={[archFilter]}
    >
      <Column headerClass="text-center" columnKey="synced" header={t("Sync")} cell={renderSyncCell} width="60px" />
      <Column columnClass="col" columnKey="channelName" header={t("Channel Name")} cell={renderChannelNameCell} />
      <Column columnClass="col col-md-2" columnKey="channelArch" header={t("Architecture")} cell={renderArchCell} />
      <Column columnClass="col col-md-3" columnKey="channelOrg" header={t("Sync Org")} cell={renderSyncOrgCell} />
    </DEPRECATED_HierarchicalTable>
  );
};

export default HierarchicalChannelsTable;
