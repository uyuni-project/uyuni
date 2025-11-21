import { FC, JSX, useMemo, useState } from "react";

import { Select } from "components/input";
import { Column, SearchField, Table, useSelected } from "components/table";

import { Channel, Org } from "./types";
import { ChannelFilter, ParentChildrenSelectionProvider } from "./utils";

export type Props = {
  channels: Channel[];
  allChannelIds: () => Promise<number[]>;
  availableOrgs: Org[];
  onChannelSyncChange: (channels: Channel[], selected: boolean) => void;
};

function getSyncedChannelIds(channels: Channel[]): number[] {
  const selectedIds = channels.filter((channel) => channel.synced).map((channel) => channel.channelId);

  for (const channel of channels) {
    if (channel.children.length > 0) {
      selectedIds.push(...getSyncedChannelIds(channel.children));
    }
  }

  return selectedIds;
}

function buildParentMap(children: Channel[], parent?: Channel): Map<string, Channel> {
  const parentMap = new Map<string, Channel>();

  children.forEach((child) => {
    if (child.parentChannelLabel !== null && child.parentChannelLabel === parent?.channelLabel) {
      parentMap.set(parent.channelLabel, parent);
    }

    if (child.children.length > 0) {
      buildParentMap(child.children, child).forEach((parent, parentLabel) => parentMap.set(parentLabel, parent));
    }
  });

  return parentMap;
}

function renderOrg(channel: Channel, selected: boolean, availableOrgs: Org[]): JSX.Element {
  if (channel.channelOrg === null) {
    // Vendor channels can't sync orgs
    return <span>{t("Vendor")}</span>;
  }

  if (channel.strictOrg && channel.selectedPeripheralOrg !== null) {
    // Only 1 option, no choice
    return <span>{channel.selectedPeripheralOrg.orgName}</span>;
  }

  if (!selected) {
    // The row is not selected, do not show anything
    return <span>{"-"}</span>;
  }

  return (
    <Select
      className="mb-0"
      name={`org-select-${channel.channelId}`}
      placeholder={t("Select Organization")}
      options={availableOrgs}
      getOptionValue={(org: Org) => org.orgId.toString()}
      getOptionLabel={(org: Org) => org.orgName}
      value={channel.selectedPeripheralOrg?.orgId.toString()}
      onChange={(newOrgId) =>
        (channel.selectedPeripheralOrg = availableOrgs.find((org) => org.orgId === Number(newOrgId)) ?? null)
      }
    />
  );
}

export const ChannelSelectorTable: FC<Props> = ({
  channels,
  allChannelIds,
  availableOrgs,
  onChannelSyncChange,
}): JSX.Element => {
  const identifier = (channel: Channel) => channel.channelId;

  const [selectedArchs, setSelectedArchs] = useState<string[]>([]);

  const availableArchitectures = useMemo(() => {
    const archSet = new Set<string>();
    channels.forEach((channel) => archSet.add(channel.channelArch));
    return Array.from(archSet).map((arch) => ({ value: arch, label: arch }));
  }, [channels]);

  const filteredChannels = useMemo(
    () => channels.filter((channel) => ChannelFilter.byArchitecture(channel, selectedArchs)),
    [channels, selectedArchs]
  );

  const parentMap = useMemo(() => buildParentMap(channels), [channels]);
  const onSyncStatusToogle = (channels: Channel[], selected: boolean): void => {
    onChannelSyncChange(channels, selected);

    // Set the first organization by default for all selected channels, if nothing is selected
    channels.forEach((channel) => {
      if (selected && channel.selectedPeripheralOrg === null && availableOrgs.length > 0) {
        channel.selectedPeripheralOrg = availableOrgs[0];
      }
    });
  };

  const syncStatus = useSelected(
    identifier,
    allChannelIds,
    new ParentChildrenSelectionProvider(parentMap),
    getSyncedChannelIds(channels),
    onSyncStatusToogle
  );

  const architectureSelector = (
    <Select
      name="channel-arch-filter"
      placeholder={t("Filter by architecture")}
      isMulti
      isClearable
      options={availableArchitectures}
      value={selectedArchs}
      onChange={setSelectedArchs}
      className="multiple-select-wrapper table-input-search"
    />
  );

  return (
    <Table
      data={filteredChannels}
      identifier={identifier}
      expandable
      searchField={<SearchField placeholder={t("Search channels...")} filter={ChannelFilter.byChannelName} />}
      additionalFilters={[architectureSelector]}
    >
      <syncStatus.Column width="60px" columnKey="synced" header={<syncStatus.Header>{t("Sync")}</syncStatus.Header>} />
      <Column
        columnClass="col"
        columnKey="channelName"
        header={t("Channel Name")}
        cell={(channel: Channel, _criteria, nestingLevel) => {
          if (nestingLevel) {
            return channel.channelName;
          }
          return <b>{channel.channelName}</b>;
        }}
      />
      <Column
        columnClass="col col-md-2"
        columnKey="channelArch"
        header={t("Architecture")}
        cell={(channel: Channel) => channel.channelArch}
      />
      <Column
        columnClass="col col-md-3"
        columnKey="channelOrg"
        header={t("Sync Org")}
        cell={(channel) => renderOrg(channel, syncStatus.isSelected(channel), availableOrgs)}
      />
    </Table>
  );
};
